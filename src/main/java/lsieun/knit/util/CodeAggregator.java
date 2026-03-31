package lsieun.knit.util;

import java.io.BufferedWriter;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.*;
import java.util.List;
import java.util.stream.Stream;

public class CodeAggregator {

    // 定义允许合并的文件后缀
    private static final List<String> ALLOWED_EXTENSIONS = List.of(".java", ".md", "pom.xml");
    private static final String OUTPUT_FILE_NAME = "Tessera-Knit-Code.md";

    public static void main(String[] args) {
        // 请替换为你项目的实际路径
        Path projectDir = Paths.get("D:\\git-repo\\Tessera-Knit");
        
        // 输出到桌面
        String userHome = System.getProperty("user.home");
        Path outputPath = Paths.get(userHome, "Desktop", OUTPUT_FILE_NAME);

        try {
            aggregateCode(projectDir, outputPath);
            System.out.println("成功！整合文件已生成至: " + outputPath);
        } catch (IOException e) {
            System.err.println("发生错误: " + e.getMessage());
            e.printStackTrace();
        }
    }

    /**
     * 核心整合方法
     * @param sourceDir 项目根目录
     * @param targetFile 输出的 MD 文件路径
     */
    public static void aggregateCode(Path sourceDir, Path targetFile) throws IOException {
        if (!Files.exists(sourceDir)) {
            throw new IOException("源目录不存在: " + sourceDir);
        }

        try (BufferedWriter writer = Files.newBufferedWriter(targetFile, StandardCharsets.UTF_8);
             Stream<Path> paths = Files.walk(sourceDir)) {

            // 写入 Markdown 文档头
            writer.write("# Tessera-Knit 项目代码整合\n");
            writer.write("> 生成时间: " + java.time.LocalDateTime.now() + "\n\n---\n\n");

            paths.filter(Files::isRegularFile)
                 .filter(CodeAggregator::isTargetFile)
                 .forEach(path -> {
                     try {
                         writePathToMarkdown(sourceDir, path, writer);
                     } catch (IOException e) {
                         System.err.println("无法读取文件: " + path);
                     }
                 });
        }
    }

    private static boolean isTargetFile(Path path) {
        String fileName = path.getFileName().toString();
        return ALLOWED_EXTENSIONS.stream().anyMatch(fileName::endsWith);
    }

    private static void writePathToMarkdown(Path root, Path file, BufferedWriter writer) throws IOException {
        // 计算相对路径，让生成的文档更整洁
        Path relativePath = root.relativize(file);
        String fileName = file.getFileName().toString();
        String extension = fileName.contains(".") ? fileName.substring(fileName.lastIndexOf(".") + 1) : "";
        
        // 特殊处理 pom.xml 的语法高亮
        String lang = fileName.equals("pom.xml") ? "xml" : extension;

        // 写入标题和代码块开头
        writer.write("## File: " + relativePath + "\n\n");
        writer.write("```" + lang + "\n");
        
        // 读取内容并写入
        Files.lines(file, StandardCharsets.UTF_8).forEach(line -> {
            try {
                writer.write(line);
                writer.newLine();
            } catch (IOException e) {
                throw new RuntimeException(e);
            }
        });

        // 写入代码块结尾
        writer.write("\n```\n\n---\n\n");
    }
}