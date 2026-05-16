package com.lox;

import com.lox.ast.Expr;
import com.lox.ast.tools.Dumper;
import com.lox.ast.tools.Printer;
import com.lox.executor.Executor;
import com.lox.lexer.Lexer;
import com.lox.lexer.LexingException;
import com.lox.parser.Parser;
import com.lox.parser.ParsingException;
import com.lox.token.TokenStream;
import com.lox.util.Position;
import com.lox.value.ReturnValue;
import com.lox.value.ThrowValue;
import com.lox.value.Value;
import java.io.BufferedReader;
import java.io.File;
import java.io.IOException;
import java.io.InputStreamReader;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.List;

public class Main {
 private static boolean printAst = false;
 private static boolean dumpAst = false;
 private static boolean verbose = false;
 private static boolean time = false;
 private static boolean compileOnly = false;
 private static String outputFile = null;
 private static String currentSource = null;
 private static String currentFilename = null;

 private static boolean interactiveMode = false;
 private static String currentDir = System.getProperty("user.dir");

 public static void main(String[] args) {
		try {
			List<String> fileArgs = parseOptions(args);

			if (fileArgs.size() == 1) {
				runFile(fileArgs.get(0));
			} else if (fileArgs.isEmpty()) {
				interactiveMode = true;
				runInteractiveMode();
			} else {
				printHelp();
				System.exit(1);
			}
		} catch (Exception e) {
			e.printStackTrace();
		}
 }

 private static void runInteractiveMode() {
		System.out.println("Lox Language v1.0 - Modo Interativo");
		System.out.println("Configurações ativas:");
		printCurrentSettings();
		System.out.println();
		System.out.println("Comandos disponíveis:");
		System.out.println("  cd <dir>     - Muda para o diretório especificado");
		System.out.println("  ls           - Lista arquivos do diretório atual");
		System.out.println("  run <file>   - Executa o arquivo .lox especificado");
		System.out.println("  set <flag>   - Ativa/desativa flags (print, dump, verbose, time)");
		System.out.println("  status       - Mostra configurações atuais");
		System.out.println("  help         - Mostra esta ajuda");
		System.out.println("  exit / quit  - Sai do programa");
		System.out.println();

		Path testsPath = Paths.get("tests");
		if (Files.exists(testsPath) && Files.isDirectory(testsPath)) {
			currentDir = testsPath.toString();
			System.out.println("Diretório atual: " + currentDir);
		} else {
			System.out.println("Diretório atual: " + currentDir);
		}
		System.out.println();

		BufferedReader reader = new BufferedReader(new InputStreamReader(System.in));

		while (true) {
			System.out.print("lox> ");
			try {
				String line = reader.readLine();
				if (line == null)
					break;

				line = line.trim();
				if (line.isEmpty())
					continue;

				if (line.equals("exit") || line.equals("quit")) {
					System.out.println("Saindo...");
					break;
				} else if (line.equals("help")) {
					printInteractiveHelp();
				} else if (line.equals("status")) {
					printCurrentSettings();
				} else if (line.startsWith("cd ")) {
					handleCdCommand(line.substring(3).trim());
				} else if (line.equals("ls")) {
					handleLsCommand();
				} else if (line.startsWith("run ")) {
					String filename = line.substring(4).trim();
					handleRunCommand(filename);
				} else if (line.startsWith("set ")) {
					handleSetCommand(line.substring(4).trim());
				} else {
					System.out.println("Comando desconhecido: " + line);
					System.out.println("Digite 'help' para ver os comandos disponíveis");
				}
			} catch (IOException e) {
				System.err.println("Erro de leitura: " + e.getMessage());
				break;
			}
		}
 }

 private static void printCurrentSettings() {
		System.out.println("  print-ast: " + (printAst ? "ativado" : "desativado"));
		System.out.println("  dump-ast:  " + (dumpAst ? "ativado" : "desativado"));
		System.out.println("  verbose:   " + (verbose ? "ativado" : "desativado"));
		System.out.println("  time:      " + (time ? "ativado" : "desativado"));
		System.out.println("  compile-only: " + (compileOnly ? "ativado" : "desativado"));
		if (outputFile != null) {
			System.out.println("  output:    " + outputFile);
		}
 }

 private static void handleSetCommand(String arg) {
		switch (arg) {
			case "print":
			case "print-ast":
				printAst = true;
				System.out.println("print-ast ativado");
				break;
			case "no-print":
			case "no-print-ast":
				printAst = false;
				System.out.println("print-ast desativado");
				break;
			case "dump":
			case "dump-ast":
				dumpAst = true;
				System.out.println("dump-ast ativado");
				break;
			case "no-dump":
			case "no-dump-ast":
				dumpAst = false;
				System.out.println("dump-ast desativado");
				break;
			case "verbose":
			case "v":
				verbose = true;
				System.out.println("verbose ativado");
				break;
			case "no-verbose":
			case "no-v":
				verbose = false;
				System.out.println("verbose desativado");
				break;
			case "time":
			case "t":
				time = true;
				System.out.println("time ativado");
				break;
			case "no-time":
			case "no-t":
				time = false;
				System.out.println("time desativado");
				break;
			case "compile-only":
			case "c":
				compileOnly = true;
				System.out.println("compile-only ativado");
				break;
			case "no-compile-only":
			case "no-c":
				compileOnly = false;
				System.out.println("compile-only desativado");
				break;
			default:
				System.out.println("Opção desconhecida: " + arg);
				System.out.println("Comandos set disponíveis:");
				System.out.println("  set print        - Ativa print da AST");
				System.out.println("  set no-print     - Desativa print da AST");
				System.out.println("  set dump         - Ativa dump da AST");
				System.out.println("  set no-dump      - Desativa dump da AST");
				System.out.println("  set verbose      - Ativa modo verboso");
				System.out.println("  set no-verbose   - Desativa modo verboso");
				System.out.println("  set time         - Ativa temporização");
				System.out.println("  set no-time      - Desativa temporização");
		}
 }

 private static void printInteractiveHelp() {
		System.out.println("Comandos disponíveis:");
		System.out.println("  cd <dir>     - Muda para o diretório especificado");
		System.out.println("                 (ex: cd .., cd tests, cd /home/user)");
		System.out.println("  ls           - Lista arquivos do diretório atual");
		System.out.println("  run <file>   - Executa o arquivo .lox especificado");
		System.out.println("  set <flag>   - Ativa/desativa flags");
		System.out.println("  status       - Mostra configurações atuais");
		System.out.println("  help         - Mostra esta ajuda");
		System.out.println("  exit / quit  - Sai do programa");
		System.out.println();
		System.out.println("Comandos 'set':");
		System.out.println("  set print        - Mostra AST em formato legível");
		System.out.println("  set dump         - Mostra AST em formato estrutura");
		System.out.println("  set verbose      - Modo verboso (tokens e debug)");
		System.out.println("  set time         - Mostra tempo de execução");
		System.out.println("  set compile-only - Apenas compila, não executa");
		System.out.println();
		System.out.println("  Use 'no-' antes da flag para desativar (ex: set no-print)");
		System.out.println();
		System.out.println("Diretório atual: " + currentDir);
 }

 private static void handleCdCommand(String dir) {
		try {
			Path newPath;
			if (dir.equals("..")) {
				newPath = Paths.get(currentDir).getParent();
				if (newPath == null) {
					System.out.println("Já está no diretório raiz");
					return;
				}
			} else if (dir.startsWith("/")) {
				newPath = Paths.get(dir);
			} else {
				newPath = Paths.get(currentDir).resolve(dir);
			}

			newPath = newPath.normalize();

			if (Files.exists(newPath) && Files.isDirectory(newPath)) {
				currentDir = newPath.toString();
				System.out.println("Diretório atual: " + currentDir);
			} else {
				System.out.println("Erro: Diretório não encontrado: " + dir);
			}
		} catch (Exception e) {
			System.out.println("Erro: " + e.getMessage());
		}
 }

 private static void handleLsCommand() {
		try {
			File dir = new File(currentDir);
			File[] files = dir.listFiles();

			if (files == null) {
				System.out.println("Erro: Não foi possível listar o diretório");
				return;
			}

			System.out.println("Arquivos em " + currentDir + ":");
			System.out.println("----------------------------------------");

			boolean hasLoxFiles = false;
			for (File file : files) {
				if (file.isFile() && file.getName().endsWith(".lox")) {
					System.out.println("  " + file.getName() + " *");
					hasLoxFiles = true;
				}
			}

			if (hasLoxFiles) {
				System.out.println("  (* arquivos .lox executáveis)");
			}

			System.out.println();
			System.out.println("Diretórios:");
			for (File file : files) {
				if (file.isDirectory()) {
					System.out.println("  [" + file.getName() + "]");
				}
			}

			System.out.println("----------------------------------------");
		} catch (Exception e) {
			System.out.println("Erro ao listar diretório: " + e.getMessage());
		}
 }

 private static void handleRunCommand(String filename) {
		try {
			Path filePath;
			if (filename.startsWith("/")) {
				filePath = Paths.get(filename);
			} else {
				filePath = Paths.get(currentDir).resolve(filename);
			}

			filePath = filePath.normalize();

			if (!Files.exists(filePath)) {
				System.out.println("Erro: Arquivo não encontrado: " + filename);
				System.out.println("Caminho procurado: " + filePath.toString());
				return;
			}

			if (!filePath.toString().endsWith(".lox")) {
				System.out.println("Aviso: O arquivo não tem extensão .lox: " + filename);
			}

			System.out.println("Executando: " + filePath.toString());
			System.out.println("----------------------------------------");

			runFile(filePath.toString());

			System.out.println("----------------------------------------");
		} catch (Exception e) {
			System.err.println("Erro ao executar arquivo: " + e.getMessage());
			if (verbose) {
				e.printStackTrace();
			}
		}
 }

 private static void printHelp() {
		System.out.println("Lox Language v1.0");
		System.out.println("Uso: java com.lox.Main [opções] <arquivo>");
		System.out.println();
		System.out.println("Sem argumentos, entra em modo interativo com comandos cd, ls, run");
		System.out.println();
		System.out.println("Opções:");
		System.out.println("  -p, --print          Mostra a AST em formato legível (código)");
		System.out.println("  -d, --dump           Mostra a AST em formato de estrutura");
		System.out.println("  --print-ast          Alias para -p");
		System.out.println("  --dump-ast           Alias para -d");
		System.out.println("  -c, --compile-only   Apenas compila, não executa");
		System.out.println("  -v, --verbose        Modo verboso (mostra tokens e debug)");
		System.out.println("  -t, --time           Mostra tempo de execução");
		System.out.println("  -o <arquivo>         Salva output em arquivo");
		System.out.println("  -h, --help           Mostra esta ajuda");
		System.out.println();
		System.out.println("Exemplos:");
		System.out.println("  java com.lox.Main script.lox");
		System.out.println("  java com.lox.Main --print-ast script.lox");
		System.out.println("  java com.lox.Main -c script.lox");
		System.out.println("  java com.lox.Main -p -o ast.txt script.lox");
		System.out.println("  java com.lox.Main                 # Modo interativo");
 }

 private static List<String> parseOptions(String[] args) {
		List<String> fileArgs = new ArrayList<>();

		for (int i = 0; i < args.length; i++) {
			switch (args[i]) {
				case "-p":
				case "--print":
				case "--print-ast":
					printAst = true;
					break;

				case "-d":
				case "--dump":
				case "--dump-ast":
					dumpAst = true;
					break;

				case "-c":
				case "--compile-only":
					compileOnly = true;
					break;

				case "-v":
				case "--verbose":
					verbose = true;
					break;

				case "-t":
				case "--time":
					time = true;
					break;

				case "-o":
					if (i + 1 < args.length) {
						outputFile = args[++i];
					} else {
						System.err.println("Erro: -o requer um nome de arquivo");
						System.exit(1);
					}
					break;

				case "-h":
				case "--help":
					printHelp();
					System.exit(0);
					break;

				default:
					if (args[i].startsWith("-")) {
						System.err.println("Opção desconhecida: " + args[i]);
						System.exit(1);
					}
					fileArgs.add(args[i]);
			}
		}

		return fileArgs;
 }

 private static void runFile(String path) throws Exception {
		byte[] bytes = Files.readAllBytes(Paths.get(path));
		String source = new String(bytes);
		currentFilename = path;

		if (verbose) {
			System.out.println("Arquivo: " + path);
			System.out.println("Tamanho: " + source.length() + " bytes");
			System.out.println("----------------------------------------");
		}

		long startTime = System.nanoTime();
		runSource(source, path);
		long endTime = System.nanoTime();

		if (time) {
			double ms = (endTime - startTime) / 1_000_000.0;
			System.out.printf("Tempo: %.2f ms%n", ms);
		}

		currentFilename = null;
 }

 private static void runSource(String source, String filename) {
		currentSource = source;
		currentFilename = filename;

		try {
			if (verbose) {
				System.out.println("Source: \"" + source + "\"");
			}

			TokenStream lexer = new Lexer(source);

			if (verbose) {
				System.out.println("Iniciando parse...");
			}

			Parser parser = new Parser(lexer);
			List<Expr> statements = parser.parse();

			if (verbose && statements != null) {
				System.out.println("Parse concluído com sucesso");
				System.out.println("Statements: " + statements.size());
			}

			StringBuilder output = new StringBuilder();

			if (printAst && statements != null && !statements.isEmpty()) {
				Printer printer = new Printer();
				StringBuilder astPrint = new StringBuilder();

				for (int i = 0; i < statements.size(); i++) {
					Expr stmt = statements.get(i);
					String printed = stmt.accept(printer);
					astPrint.append(printed);
					if (i < statements.size() - 1) {
						astPrint.append("\n");
					}
				}

				output.append("AST Print:\n").append(astPrint.toString()).append("\n");
				System.out.println("AST Print:\n" + astPrint.toString());
			}

			if (dumpAst && statements != null && !statements.isEmpty()) {
				Dumper dumper = new Dumper();
				StringBuilder astDump = new StringBuilder();

				for (int i = 0; i < statements.size(); i++) {
					Expr stmt = statements.get(i);
					String dumped = stmt.accept(dumper);
					astDump.append(dumped);
					if (i < statements.size() - 1) {
						astDump.append("\n");
					}
				}

				output.append("AST Dump:\n").append(astDump.toString()).append("\n");
				System.out.println("AST Dump:\n" + astDump.toString());
			}

			if (outputFile != null && output.length() > 0) {
				Files.write(Paths.get(outputFile), output.toString().getBytes());
				if (verbose) {
					System.out.println("AST salva em: " + outputFile);
				}
			}

			if (statements == null || statements.isEmpty()) {
				if (verbose) {
					System.out.println("Nenhum statement para executar");
				}
				return;
			}

			if (compileOnly) {
				if (verbose) {
					System.out.println("Modo compilar apenas - execução pulada");
				}
				return;
			}

			if (verbose) {
				System.out.println("Executando...");
			}

			Executor executor = new Executor();
			Value result = executor.execute(statements);

			if (result != null) {
				if (result.isNull()) {
					// Não imprime nada
				} else if (result.isReturn()) {
					if (verbose) {
						System.err.println("Aviso: return não capturado no nível global");
					}
					ReturnValue ret = result.asReturn();
					Value value = ret.getValue();
					if (!value.isNull()) {
						System.out.println(value.toString());
					}
				} else if (result.isThrow()) {
					ThrowValue throwValue = result.asThrow();
					System.err.println("Exceção não capturada: " + throwValue.getValue().toString());
				} else {
					System.out.println(result.toString());
				}
			}

		} catch (ParsingException e) {
			printParsingError(e, source, filename);
		} catch (LexingException e) {
			printLexingError(e, source, filename);
		} catch (Exception e) {
			System.err.println("Erro: " + e.getMessage());
			if (verbose) {
				e.printStackTrace();
			}
		}

		currentSource = null;
		currentFilename = null;
 }

 private static void printLexingError(LexingException e, String source, String filename) {
		Position pos = e.getPosition();
		String location = filename != null ? filename : "linha";

		if (pos != null && pos.getLine() > 0) {
			int line = pos.getLine();
			int column = pos.getColumn();

			System.err.println("Erro de lexing em " + location + " " + line + ":" + column);
			System.err.println(e.getMessage());

			if (source != null && line > 0) {
				String[] lines = source.split("\n", -1);
				if (line <= lines.length) {
					String errorLine = lines[line - 1];
					System.err.println("    " + errorLine);

					if (column > 0 && column <= errorLine.length()) {
						StringBuilder pointer = new StringBuilder();
						pointer.append("    ");
						for (int i = 1; i < column; i++) {
							pointer.append(" ");
						}
						pointer.append("^");
						System.err.println(pointer.toString());
					}
				}
			}
		} else {
			System.err.println("Erro de lexing: " + e.getMessage());
		}

		if (verbose) {
			e.printStackTrace();
		}
 }

 private static void printParsingError(ParsingException e, String source, String filename) {
		Position pos = e.getPosition();
		String location = filename != null ? filename : "linha";

		if (pos != null && pos.getLine() > 0) {
			int line = pos.getLine();
			int column = pos.getColumn();
			int start = pos.getStart();
			int end = pos.getEnd();

			System.err.println("Erro de parsing em " + location + " " + line + ":" + column);
			System.err.println(e.getMessage());

			if (source != null && line > 0) {
				String[] lines = source.split("\n", -1);
				if (line <= lines.length) {
					String errorLine = lines[line - 1];
					System.err.println("    " + errorLine);

					if (start >= 0 && end >= 0 && start < source.length()) {
						int lineStart = findLineStart(source, line);
						int columnStart = start - lineStart;
						int columnEnd = end - lineStart;

						if (columnStart >= 0 && columnEnd >= columnStart) {
							StringBuilder pointer = new StringBuilder();
							pointer.append("    ");

							for (int i = 0; i < columnStart && i < errorLine.length(); i++) {
								pointer.append(" ");
							}

							int pointerEnd = Math.min(columnEnd, errorLine.length());
							for (int i = columnStart; i < pointerEnd; i++) {
								pointer.append("^");
							}

							if (pointer.length() > 4) {
								System.err.println(pointer.toString());
							}
						}
					} else if (column > 0 && column <= errorLine.length()) {
						StringBuilder pointer = new StringBuilder();
						pointer.append("    ");
						for (int i = 1; i < column; i++) {
							pointer.append(" ");
						}
						pointer.append("^");
						System.err.println(pointer.toString());
					}
				}
			}
		} else {
			System.err.println("Erro de parsing: " + e.getMessage());
		}

		if (verbose) {
			e.printStackTrace();
		}
 }

 private static int findLineStart(String source, int line) {
		int pos = 0;
		for (int i = 1; i < line; i++) {
			pos = source.indexOf('\n', pos);
			if (pos == -1)
				break;
			pos++;
		}
		return pos;
 }
}