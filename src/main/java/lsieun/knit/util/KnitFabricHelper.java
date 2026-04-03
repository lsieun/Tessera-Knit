package lsieun.knit.util;

import lsieun.knit.event.KnitEventConverter;
import lsieun.knit.converter.FabricConverter;
import lsieun.knit.engine.KnitEngine;
import lsieun.knit.event.KnitEvent;
import lsieun.knit.model.KnitCourse;
import lsieun.knit.model.KnitFabric;

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

    public static KnitFabric getFabric(KnitEngine engine, String fabricName) {
        List<KnitEvent> eventLog = engine.getEventLog();
        KnitEventConverter<KnitFabric> converter = new FabricConverter(fabricName);
        return converter.convert(eventLog);
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