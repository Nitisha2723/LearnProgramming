# Setup Guide — Learn Java Repository

This guide gets your Java development environment ready from scratch. Follow the steps for your operating system.

---

## 1. Install Java 21 (JDK)

Java 21 is the current Long-Term Support (LTS) release. Always install a JDK (Java Development Kit), not just a JRE (Java Runtime Environment) — the JDK includes the compiler (`javac`) that you need.

### macOS
```bash
# Using Homebrew (recommended)
brew install openjdk@21

# Then add Java to your PATH (add this line to ~/.zshrc or ~/.bash_profile):
export PATH="/opt/homebrew/opt/openjdk@21/bin:$PATH"

# Reload your shell config
source ~/.zshrc
```

Alternatively, download the macOS installer directly from:
https://adoptium.net/temurin/releases/?version=21

### Windows
1. Download the Windows installer from https://adoptium.net/temurin/releases/?version=21
2. Run the installer and check "Set JAVA_HOME variable" and "Add to PATH" during setup.
3. Open a new Command Prompt and verify: `java -version`

### Linux (Ubuntu/Debian)
```bash
sudo apt update
sudo apt install openjdk-21-jdk

# Verify
java -version
```

### Verify your installation
```
java -version
javac -version
```
Both commands should show version 21.x.x.

---

## 2. Install Maven

Maven is a build tool that manages dependencies and runs tests automatically. It downloads JUnit and Mockito for you so you do not have to manage JAR files manually.

### macOS
```bash
brew install maven

# Verify
mvn -version
```

### Windows
1. Download the binary ZIP from https://maven.apache.org/download.cgi
2. Unzip to a folder, e.g., `C:\Program Files\Apache\maven`
3. Add `C:\Program Files\Apache\maven\bin` to your PATH environment variable.
4. Open a new Command Prompt and verify: `mvn -version`

### Linux (Ubuntu/Debian)
```bash
sudo apt install maven

# Verify
mvn -version
```

---

## 3. Run All Tests

From the root of this repository (the folder containing `pom.xml`):

```bash
mvn test
```

Maven will:
1. Download all dependencies (JUnit 5, Mockito) on first run — this requires internet access.
2. Compile all Java source files.
3. Run every test class found under `src/test/java`.
4. Print a summary: how many tests ran, how many passed, how many failed.

To run tests for a specific module only:
```bash
mvn test -pl 01-foundations
```

To skip tests (compile only):
```bash
mvn compile -DskipTests
```

---

## 4. Compile and Run a Single File

For quick experiments without Maven, you can compile and run individual `.java` files directly.

### Compile
```bash
javac FileName.java
```
This produces a `FileName.class` bytecode file.

### Run
```bash
java ClassName
```
Note: use the class name (without `.class`), not the filename.

### Example
```bash
# In 01-foundations/code/
javac HelloWorld.java
java HelloWorld
```

### With a package
If your file has a `package com.example;` declaration, compile from the project root:
```bash
javac -d out src/com/example/HelloWorld.java
java -cp out com.example.HelloWorld
```

---

## 5. IDE Setup

An IDE (Integrated Development Environment) makes Java development much more productive. It gives you code completion, inline error highlighting, and one-click test running.

### IntelliJ IDEA (Recommended)

IntelliJ IDEA is the most popular Java IDE and gives the best experience for this repository.

1. Download the free **Community Edition** from https://www.jetbrains.com/idea/download/
2. Open IntelliJ IDEA and select **Open**, then navigate to the `learn-java` directory.
3. IntelliJ detects the `pom.xml` and imports the project automatically.
4. To run tests: right-click any test file or the `tests/` folder and choose **Run Tests**.
5. JDK setup: go to **File > Project Structure > SDKs** and point it at your Java 21 installation.

Key shortcuts:
- `Ctrl+Shift+F10` (Windows/Linux) / `Ctrl+Shift+R` (macOS): Run the current file
- `Ctrl+Shift+T`: Create or navigate to a test
- `Shift+F10`: Re-run last configuration

### VS Code with Extension Pack for Java

VS Code is a lighter alternative that works well for this repository.

1. Download VS Code from https://code.visualstudio.com/
2. Open the Extensions panel (`Ctrl+Shift+X`) and search for **Extension Pack for Java** (publisher: Microsoft).
3. Install the pack — it includes the Language Server, Debugger, Test Runner, Maven support, and more.
4. Open the `learn-java` folder in VS Code (**File > Open Folder**).
5. VS Code detects the `pom.xml` and configures the project automatically.
6. To run tests: click the beaker icon in the sidebar (Testing view), then click the play button next to any test.

Required extensions (all included in the Extension Pack):
- Language Support for Java (Red Hat)
- Debugger for Java (Microsoft)
- Test Runner for Java (Microsoft)
- Maven for Java (Microsoft)
- Project Manager for Java (Microsoft)

---

## Quick Reference

| Task | Command |
|---|---|
| Run all tests | `mvn test` |
| Compile a file | `javac FileName.java` |
| Run a class | `java ClassName` |
| Check Java version | `java -version` |
| Check Maven version | `mvn -version` |
| Clean build output | `mvn clean` |
| Compile without running tests | `mvn compile` |
