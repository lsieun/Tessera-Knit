package lsieun.knit.util;

import lsieun.knit.event.KnitEvent;

import java.util.List;

/**
 * KnitEvent 打印工具类
 * 负责将事件流水账以格式化的表格形式输出到控制台
 */
public final class KnitEventPrinter
{

    private KnitEventPrinter()
    {
        throw new UnsupportedOperationException("Utility class");
    }

    /**
     * 打印完整的事件日志
     */
    public static void print(List<KnitEvent> events)
    {
        if (events == null || events.isEmpty()) {
            System.out.println("No events to print.");
            return;
        }

        // 打印表头
        System.out.println("-".repeat(105));
        System.out.printf("| %-15s | %-10s | %-8s | %-15s | %-6s | %-8s |%n",
                "Instruction", "Binding", "Action", "Stitch Change", "Pass", "Course");
        System.out.println("-".repeat(105));

        // 打印行
        for (KnitEvent e : events) {
            String insName = e.instruction().getName();
            // 逻辑位置格式化: R(Repeat) P(Pass)
            String binding = String.format("R%d-P%d",
                    e.binding().repeatIndex(), e.binding().passIndexInRepeat());

            String action = e.isPhysical() ? "KNIT" : "ADJUST";

            // 针数变化
            String stitchFlow;
            if (e.isAdjustmentOnly()) {
                stitchFlow = String.format("%3d -> %-3d", e.stitchesBefore(), e.stitchesAfter());
            }
            else {
                // 使用 7 个空格（" -> " 的长度）来补位，让单数字也能和上面的起始数字对齐
                stitchFlow = String.format("%3d       ", e.stitchesBefore());
            }

            // 物理行程信息
            String passType = e.pass().map(p -> p.passType().toString()).orElse("-");
            String courseIdx = e.pass().map(p -> String.valueOf(p.courseIndex())).orElse("-");

            System.out.printf("| %-15s | %-10s | %-8s | %-15s | %-6s | %-8s |%n",
                    insName, binding, action, stitchFlow, passType, courseIdx);
        }
        System.out.println("-".repeat(105));
        System.out.printf("Total Events: %d%n", events.size());
    }
}