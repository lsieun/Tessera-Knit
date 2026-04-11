package lsieun.knit.util;

import lsieun.knit.event.KnitEventConverter;
import lsieun.knit.converter.FabricConverter;
import lsieun.knit.engine.KnitEngine;
import lsieun.knit.event.KnitEvent;
import lsieun.knit.model.KnitCourse;
import lsieun.knit.model.KnitFabric;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.List;

/**
 * KnitFabric 静态工具类
 * 采用私有构造函数防止实例化，所有方法均为静态。
 */
public final class KnitFabricHelper
{

    // 1. 私有构造函数，防止在外部 new KnitFabricHelper()
    private KnitFabricHelper()
    {
        throw new UnsupportedOperationException("Utility class cannot be instantiated");
    }

    public static KnitFabric getFabric(KnitEngine engine, String fabricName)
    {
        List<KnitEvent> eventLog = engine.getEventLog();
        KnitEventConverter<KnitFabric> converter = new FabricConverter(fabricName);
        return converter.convert(eventLog);
    }

    /**
     * 以字符阵列形式打印织物形状（物理预览）
     * 规则：1针 = 1个字符 "."
     * 顺序：Course 1 在最底行，最新 Course 在最顶行
     */
    public static void printVisualShape(KnitFabric fabric)
    {
        if (fabric == null || fabric.courses().isEmpty()) {
            System.out.println("Empty Fabric.");
            return;
        }

        // 1. 获取全局最大针数，用于确定画布宽度
        int maxWidth = getMaxStitchCount(fabric);
        List<KnitCourse> courses = fabric.courses();

        System.out.println("--- Visual Shape Preview (Bottom to Top) ---");

        // 2. 倒序遍历 Course（从最后一个 Course 开始打印，保证 Course 1 在最下面）
        for (int i = courses.size() - 1; i >= 0; i--) {
            KnitCourse c = courses.get(i);

            // 按照编织顺序的逆序打印：先打印 Back，后打印 Go（因为 Back 在上面）
            c.getBackPass().ifPresent(p -> printCenteredPass(p.stitchCount(), maxWidth, "B"));
            c.getGoPass().ifPresent(p -> printCenteredPass(p.stitchCount(), maxWidth, "G"));
        }

        System.out.println("-".repeat(maxWidth + 10));
    }

    /**
     * 辅助方法：居中打印单行 Pass
     *
     * @param count       当前 Pass 的针数
     * @param canvasWidth 画布总宽度（最大针数）
     * @param label       侧边标识（G 代表 Go, B 代表 Back）
     */
    private static void printCenteredPass(int count, int canvasWidth, String label)
    {
        int padding = (canvasWidth - count) / 2;

        // 打印左侧标签和缩进
        System.out.print(String.format("[%s] ", label));
        System.out.print(" ".repeat(padding));

        // 打印针迹
        System.out.print(".".repeat(count));

        // 换行
        System.out.println();
    }

    /**
     * 生成织物形状的 SVG 预览文件
     * @param fabric 织物对象
     * @param filePath 文件输出路径 (例如 "fabric-preview.svg")
     */
    public static void saveAsSvg(KnitFabric fabric, String filePath) {
        if (fabric == null || fabric.courses().isEmpty()) return;

        // 1. 定义物理常量 (单位：像素)
        int stitchWidth = 4;  // 每一针的宽度
        int rowHeight = 4;    // 每一行（Pass）的高度
        int maxWidth = getMaxStitchCount(fabric);

        List<KnitCourse> courses = fabric.courses();
        int totalPasses = getTotalPasses(fabric);

        // 2. 计算画布尺寸
        int canvasWidth = maxWidth * stitchWidth + 40; // 左右各留 20px 边距
        int canvasHeight = totalPasses * rowHeight + 40; // 上下各留 20px 边距

        StringBuilder svg = new StringBuilder();
        // SVG Header
        svg.append(String.format("<svg width=\"%d\" height=\"%d\" xmlns=\"http://www.w3.org/2000/svg\">\n", canvasWidth, canvasHeight));
        svg.append("  <rect width=\"100%\" height=\"100%\" fill=\"#f8f9fa\" />\n"); // 背景色

        // 3. 绘制逻辑
        int currentPassIndex = 0; // 用于计算纵向偏移

        // 按 Course 顺序从 1 到 N 遍历
        for (KnitCourse c : courses) {
            // 一个 Course 可能有 Go 和 Back 两个行程
            // 按照编织顺序绘制：Go 先织（在下面），Back 后织（在上面）
            if (c.getGoPass().isPresent()) {
                drawPass(svg, c.getGoPass().get().stitchCount(), maxWidth, currentPassIndex++, canvasHeight, stitchWidth, rowHeight, "#2c3e50");
            }
            if (c.getBackPass().isPresent()) {
                drawPass(svg, c.getBackPass().get().stitchCount(), maxWidth, currentPassIndex++, canvasHeight, stitchWidth, rowHeight, "#e74c3c");
            }
        }

        svg.append("</svg>");

        // 4. 写入文件
        try {
            Files.writeString(Paths.get(filePath), svg.toString());
            System.out.println("SVG preview generated: " + filePath);
        } catch (IOException e) {
            System.err.println("Failed to write SVG: " + e.getMessage());
        }
    }

    private static void drawPass(StringBuilder svg, int count, int maxWidth, int passIdx,
                                 int canvasHeight, int sw, int rh, String color) {
        // 计算居中偏移
        float offsetX = (maxWidth - count) / 2.0f * sw + 20;
        // 计算纵向位置（从下往上堆叠）
        float offsetY = canvasHeight - (passIdx + 1) * rh - 20;

        // 绘制代表该行程的矩形条
        svg.append(String.format("  <rect x=\"%.2f\" y=\"%.2f\" width=\"%d\" height=\"%d\" rx=\"2\" fill=\"%s\" opacity=\"0.8\">\n",
                offsetX, offsetY, count * sw, rh - 2, color));
        svg.append(String.format("    <title>Stitches: %d</title>\n", count));
        svg.append("  </rect>\n");
    }

    // --- 静态统计方法 ---

    public static int getTotalPasses(KnitFabric fabric)
    {
        if (fabric == null) return 0;
        return fabric.courses().stream()
                .mapToInt(c -> (c.getGoPass().isPresent() ? 1 : 0) + (c.getBackPass().isPresent() ? 1 : 0))
                .sum();
    }

    public static int getMaxStitchCount(KnitFabric fabric)
    {
        if (fabric == null) return 0;
        return fabric.courses().stream()
                .flatMap(c -> List.of(c.getGoPass(), c.getBackPass()).stream())
                .filter(java.util.Optional::isPresent)
                .mapToInt(p -> p.get().stitchCount())
                .max()
                .orElse(0);
    }

    // --- 静态报表方法 ---

    public static String toMarkdown(KnitFabric fabric)
    {
        if (fabric == null) return "";

        StringBuilder sb = new StringBuilder();
        sb.append("### Fabric Report: ").append(fabric.name()).append("\n\n");
        sb.append("| Course | Go (Stitch) | Back (Stitch) | Total |\n");
        sb.append("| :--- | :--- | :--- | :--- |\n");

        List<KnitCourse> courses = fabric.courses();
        for (int i = courses.size() - 1; i >= 0; i--) {
            KnitCourse c = courses.get(i);
            String go = c.getGoPass().map(p -> String.valueOf(p.stitchCount())).orElse("-");
            String back = c.getBackPass().map(p -> String.valueOf(p.stitchCount())).orElse("-");
            int passCount = (c.getGoPass().isPresent() ? 1 : 0) + (c.getBackPass().isPresent() ? 1 : 0);

            sb.append(String.format("| %d | %s | %s | %d |\n",
                    c.getCourseIndex(), go, back, passCount));
        }
        return sb.toString();
    }

    public static String toConsoleSummary(KnitFabric fabric)
    {
        if (fabric == null) return "Fabric is null";
        return String.format("Fabric [%s]: %d Courses, %d Passes, Max Width: %d stitches.",
                fabric.name(),
                fabric.courses().size(),
                getTotalPasses(fabric),
                getMaxStitchCount(fabric));
    }
}