package lsieun.knit.simulator;

import java.util.ArrayList;
import java.util.List;

public class KnittingSimulator {

    // 定义策略：先动作再编织（如 6+1+30）或先编织再动作（如 7+1+10）
    enum Strategy {
        ACTION_THEN_KNIT, // 先加减针，再织间隔转数
        KNIT_THEN_ACTION  // 先织间隔转数，再加减针
    }

    // 工艺阶段数据模型
    static class KnittingCommand {
        String name;          // 阶段名称
        int interval;         // 间隔转数 (round)
        int needleAdjust;     // 单边加减针数 (needles)
        int repeats;          // 执行次数 (n)
        Strategy strategy;    // 计算策略

        public KnittingCommand(String name, int interval, int needleAdjust, int repeats, Strategy strategy) {
            this.name = name;
            this.interval = interval;
            this.needleAdjust = needleAdjust;
            this.repeats = repeats;
            this.strategy = strategy;
        }
    }

    // 表格行模型
    static class TableRow {
        String stage, desc, loop, logic;
        int startN, usedC, endN, totalC;

        public String toHtml() {
            return String.format("<tr><td>%s</td><td>%s</td><td>%s</td><td>%d</td><td>%s</td><td>%d</td><td>%d</td><td>%d</td></tr>",
                    stage, desc, loop, startN, logic, usedC, endN, totalC);
        }
    }

    public static void main(String[] args) {
        int currentNeedles = 267; // 初始针数
        int totalCourses = 0;    // 累计转数
        List<TableRow> rows = new ArrayList<>();

        // 1. 基础平织 6 转
        TableRow start = new TableRow();
        start.stage = "起始段";
        start.desc = "基础平织";
        start.loop = "-";
        start.startN = currentNeedles;
        start.logic = "无变化";
        start.usedC = 6;
        totalCourses += 6;
        start.endN = currentNeedles;
        start.totalC = totalCourses;
        rows.add(start);

        // 2. 指令集定义
        List<KnittingCommand> commands = new ArrayList<>();
        commands.add(new KnittingCommand("袖身放针 A", 6, 1, 30, Strategy.ACTION_THEN_KNIT));
        commands.add(new KnittingCommand("袖身放针 B", 7, 1, 10, Strategy.ACTION_THEN_KNIT)); // 此处可根据实际灵活调整策略
        // 注意：根据你的推论 6+(6*29)+(7*10)=250，这里 B 段应为 KNIT_THEN_ACTION

        // 模拟执行逻辑
        for (KnittingCommand cmd : commands) {
            for (int i = 1; i <= cmd.repeats; i++) {
                TableRow row = new TableRow();
                row.stage = cmd.name;
                row.desc = String.format("%d+%d+%d", cmd.interval, cmd.needleAdjust, cmd.repeats);
                row.loop = i + "/" + cmd.repeats;
                row.startN = currentNeedles;

                if (cmd.strategy == Strategy.ACTION_THEN_KNIT) {
                    // 先加针
                    currentNeedles += (cmd.needleAdjust * 2);
                    row.logic = "先加后织";
                    // 如果是最后一次且为了凑 250 转逻辑 (n-1)，此处可加判断
                    int coursesThisLoop = (i == cmd.repeats && cmd.name.contains("A")) ? 0 : cmd.interval;
                    totalCourses += coursesThisLoop;
                    row.usedC = coursesThisLoop;
                } else {
                    // 先织再加
                    row.logic = "先织后加";
                    totalCourses += cmd.interval;
                    currentNeedles += (cmd.needleAdjust * 2);
                    row.usedC = cmd.interval;
                }

                row.endN = currentNeedles;
                row.totalC = totalCourses;
                rows.add(row);
            }
        }

        // 输出 HTML
        printHtmlTable(rows);
    }

    private static void printHtmlTable(List<TableRow> rows) {
        System.out.println("<table border='1' style='border-collapse:collapse; width:100%; text-align:center;'>");
        System.out.println("<tr style='background-color:#f2f2f2;'><th>工艺阶段</th><th>指令描述</th><th>循环/序号</th><th>起始针数</th><th>变化逻辑</th><th>耗费转数</th><th>最终针数</th><th>累计总转数</th></tr>");
        for (TableRow r : rows) System.out.println(r.toHtml());
        System.out.println("</table>");
    }
}