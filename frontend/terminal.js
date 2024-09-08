// Initialize Ace Editor
let editor = ace.edit("editor");
editor.setTheme("ace/theme/merbivore");
editor.session.setMode("ace/mode/python");

// API URL
const apiUrl = 'http://localhost:8081/api/code/';

// Code snippets for each language
const codeSnippets = {
    python: `# Python code example\nprint("CodeNest")`,
    javascript: `// JavaScript code example\nconsole.log("CodeNest");`,
    java: `// Java code example\npublic class Main {\n    public static void main(String[] args) {\n        System.out.println("CodeNest");\n    }\n}`,
    cpp: `// C++ code example\n#include <iostream>\n\nint main() {\n    std::cout << "CodeNest" << std::endl;\n    return 0;\n}`
};

// Function to set the editor content based on the selected language
function setCodeSnippet(language) {
    const snippet = codeSnippets[language] || '';
    editor.setValue(snippet, -1); // -1 to move the cursor to the beginning
}

// Function to execute the code and fetch output
function executeCode() {
    const code = editor.getValue().trim(); // Get the code from Ace Editor
    const language = document.getElementById('language').value;
    const input = document.getElementById('userInput').value;

    fetch(apiUrl + 'execute', {
        method: 'POST',
        headers: {
            'Content-Type': 'application/x-www-form-urlencoded'
        },
        body: `language=${encodeURIComponent(language)}&code=${encodeURIComponent(code)}&input=${encodeURIComponent(input)}`
    })
    .then(response => response.text()) // Parse response as text
    .then(id => {
        document.getElementById('output').innerText = 'Code is running...';
        document.getElementById('output').setAttribute('data-execution-id', id); // Store execution ID
    })
    .catch(error => {
        console.error('Error executing code:', error);
        document.getElementById('output').innerText = 'Error executing code.';
    });
}

// Function to fetch output from the database
function fetchOutput() {
    const executionId = document.getElementById('output').getAttribute('data-execution-id');
    if (!executionId) {
        console.error('No execution ID available.');
        document.getElementById('output').innerText = 'No execution ID available.';
        return;
    }

    fetch(apiUrl + 'output?id=' + encodeURIComponent(executionId))
    .then(response => response.text()) // Parse response as text
    .then(output => {
        document.getElementById('output').innerText = output;
    })
    .catch(error => {
        console.error('Error fetching output:', error);
        document.getElementById('output').innerText = 'Error fetching output.';
    });
}

// Function to handle Run button click
function Run() {
    executeCode();
    setTimeout(fetchOutput, 2000); // Fetch output after 2 seconds
}

// Handle language changes
document.getElementById('language').addEventListener('change', function() {
    const mode = this.value;
    switch (mode) {
        case 'python':
            editor.session.setMode("ace/mode/python");
            break;
        case 'javascript':
            editor.session.setMode("ace/mode/javascript");
            break;
        case 'java':
            editor.session.setMode("ace/mode/java");
            break;
        case 'cpp':
            editor.session.setMode("ace/mode/c_cpp");
            break;
        default:
            editor.session.setMode("ace/mode/text");
    }
    setCodeSnippet(mode); // Update the editor with the selected language's snippet
});

// Initialize with default language snippet
setCodeSnippet('python');
