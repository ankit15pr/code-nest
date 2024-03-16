package com.api.codenest;

import java.io.BufferedReader;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.io.PrintStream;
import java.lang.reflect.Method;
import java.net.URI;
import java.net.URL;
import java.net.URLClassLoader;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.sql.SQLException;
import java.util.Optional;
import java.util.Random;
import java.util.concurrent.CompletableFuture;

import javax.tools.JavaCompiler;
import javax.tools.SimpleJavaFileObject;
import javax.tools.StandardJavaFileManager;
import javax.tools.ToolProvider;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/code")
public class Controller {

	@Autowired
	private CodeExecutorService codeSummaryService;

	private static final String CHARACTERS = "ABCDEFGHIJKLMNOPQRSTUVWXYZabcdefghijklmnopqrstuvwxyz0123456789";

	@PostMapping("/execute")
	public ResponseEntity<String> executeCode(@RequestParam String language, @RequestParam String code) {
		System.out.println("Execute code called for language " + language);
		String id = generateRandomString(5);
<<<<<<< Updated upstream
=======

		// Save the language and code into MySQL
		try (Connection conn = DriverManager.getConnection("jdbc:mysql://localhost:3306/codenest", "root", "root")) {
			String sql = "INSERT INTO codesummary (id, language, code) VALUES (?, ?, ?)";
			try (PreparedStatement pstmt = conn.prepareStatement(sql)) {
				pstmt.setString(1, id);
				pstmt.setString(2, language);
				pstmt.setString(3, code);
				pstmt.executeUpdate();
			}
		} catch (SQLException e) {
			e.printStackTrace();
			return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body("Error saving code to database");
		}

>>>>>>> Stashed changes
		CompletableFuture.runAsync(() -> {

			switch (language.toLowerCase()) {
			case "java":
<<<<<<< Updated upstream
				executeJavaCode(code, id);
				break;
			case "python":
				executePythonCode(code, id);
				break;
			case "cpp":
				executeCppCode(code, id);
=======
				executeJavaCode(id);
				break;
			case "python":
				executePythonCode(id);
				break;
			case "cpp":
				executeCppCode(id);
>>>>>>> Stashed changes
				break;
			// Add cases for other languages as needed
			}
		});
		return ResponseEntity.ok(id);
	}

	@GetMapping("/output")
	public ResponseEntity<String> getOutput(@RequestParam String id) {
		String output = codeSummaryService.getFieldById(id).get().getOutput();
		if (output == null) {
			return ResponseEntity.notFound().build();
		}
		return ResponseEntity.ok(output);
	}

	static class StringSourceJavaObject extends SimpleJavaFileObject {
		private final String code;
		private final ByteArrayOutputStream outputStream = new ByteArrayOutputStream();

		public StringSourceJavaObject(String name, String code) {
			super(URI.create("string:///" + name.replace('.', '/') + Kind.SOURCE.extension), Kind.SOURCE);
			this.code = code;
		}

		@Override
		public CharSequence getCharContent(boolean ignoreEncodingErrors) {
			return code;
		}

		public String getOutput() {
			return outputStream.toString();
		}

		@Override
		public OutputStream openOutputStream() {
			return outputStream;
		}
	}

	private void executeJavaCode(String id) {
		String output;
		try {
			Optional<CodeSummaryEntity> res = codeSummaryService.getFieldById(id);
			Path sourcePath = Paths.get("Main.java");
			Files.write(sourcePath, res.get().getCode().getBytes());

			// Redirect standard output to capture the response
			ByteArrayOutputStream outputStream = new ByteArrayOutputStream();
			PrintStream printStream = new PrintStream(outputStream);
			PrintStream standardOut = System.out;
			System.setOut(printStream);

			// Compile the Java file
			JavaCompiler compiler = ToolProvider.getSystemJavaCompiler();
			StandardJavaFileManager fileManager = compiler.getStandardFileManager(null, null, null);
			compiler.getTask(null, fileManager, null, null, null, fileManager.getJavaFileObjects(sourcePath)).call();
			fileManager.close();

			// Load and execute the compiled class
			URLClassLoader classLoader = URLClassLoader.newInstance(new URL[] { new File("").toURI().toURL() });
			Class<?> clazz = Class.forName("Main", true, classLoader);
			Method method = clazz.getMethod("main", String[].class);
			method.invoke(null, new Object[] { null });

			// Reset standard output
			System.out.flush();
			System.setOut(standardOut);

			// Capture the output
			output = outputStream.toString();

		} catch (Exception e) {
			System.out.println(e.toString());
			output = e.toString();
		}
		codeSummaryService.updateFieldById(id, output);
	}

	public static String generateRandomString(int length) {
		Random random = new Random();
		StringBuilder sb = new StringBuilder(length);

		for (int i = 0; i < length; i++) {
			int randomIndex = random.nextInt(CHARACTERS.length());
			char randomChar = CHARACTERS.charAt(randomIndex);
			sb.append(randomChar);
		}

		return sb.toString();
	}

	private void executePythonCode(String id) {
		String output;
		try {
			// Write the Python code to a file
			Optional<CodeSummaryEntity> res = codeSummaryService.getFieldById(id);
			Path sourcePath = Paths.get("main.py");
			Files.write(sourcePath, res.get().getCode().getBytes());

			// Execute the Python code
			Process process = new ProcessBuilder("python", sourcePath.toString()).start();
			BufferedReader reader = new BufferedReader(new InputStreamReader(process.getInputStream()));
			StringBuilder result = new StringBuilder();
			String line;
			while ((line = reader.readLine()) != null) {
				result.append(line).append("\n");
			}

			// Wait for the process to complete and capture any errors
			int exitCode = process.waitFor();
			if (exitCode == 0) {
				output = result.toString();
			} else {
				BufferedReader errorReader = new BufferedReader(new InputStreamReader(process.getErrorStream()));
				StringBuilder errorResult = new StringBuilder();
				String errorLine;
				while ((errorLine = errorReader.readLine()) != null) {
					errorResult.append(errorLine).append("\n");
				}
				output = "Error executing Python code:\n" + errorResult.toString();
			}
		} catch (Exception e) {
			output = "Error executing Python code:\n" + e.toString();
		}
		codeSummaryService.updateFieldById(id, output);
	}

	private void executeCppCode(String id) {
		String output;
		try {
			Optional<CodeSummaryEntity> res = codeSummaryService.getFieldById(id);
			// Write the C++ code to a file
			Path sourcePath = Paths.get("main.cpp");
			Files.write(sourcePath, res.get().getCode().getBytes());

			// Compile the C++ code
			Process compileProcess = new ProcessBuilder("g++", sourcePath.toString(), "-o", "main").start();
			compileProcess.waitFor();

			// Execute the compiled C++ code
			Process executeProcess = new ProcessBuilder("./main").start();
			BufferedReader reader = new BufferedReader(new InputStreamReader(executeProcess.getInputStream()));
			StringBuilder result = new StringBuilder();
			String line;
			while ((line = reader.readLine()) != null) {
				result.append(line).append("\n");
			}

			// Wait for the process to complete and capture any errors
			int exitCode = executeProcess.waitFor();
			if (exitCode == 0) {
				output = result.toString();
			} else {
				BufferedReader errorReader = new BufferedReader(new InputStreamReader(executeProcess.getErrorStream()));
				StringBuilder errorResult = new StringBuilder();
				String errorLine;
				while ((errorLine = errorReader.readLine()) != null) {
					errorResult.append(errorLine).append("\n");
				}
				output = "Error executing C++ code:\n" + errorResult.toString();
			}
		} catch (Exception e) {
			output = "Error executing C++ code:\n" + e.toString();
		}
		codeSummaryService.updateFieldById(id, output);
	}
}
