package lsieun.knit.simulator;

import java.util.ArrayList;
import java.util.List;

public class KnittingTechnician {

    // 模拟器状态追踪
    static class MachineState {
        int currentNeedle = 267;
        int currentLine = 17; // 假设从18行开始（17行之前为废纱）
        double totalCourses = 0;
    }

    static class ProcessStep {
        String stage, instruction, loop, logic;
        String startNStr, endNStr;
        double courseUsed;
        int lineStart, lineEnd;
        boolean isHeader = false;

        public String toHtmlRow(boolean printStage, int stageSpan) {
            String stageTd = printStage ? String.format("<td rowspan=\"%d\">%s</td>", stageSpan, stage) : "";
            return String.format("<tr>%s<td>%s</td><td>%s</td><td>%s</td><td>%s</td><td>%.1f</td><td>%d - %d (%d行)</td><td>%s</td></tr>",
                    stageTd, instruction, loop, startNStr, logic, courseUsed, lineStart, lineEnd, (lineEnd - lineStart + 1), endNStr);
        }
    }

    public static void main(String[] args) {
        MachineState state = new MachineState();
        List<ProcessStep> report = new ArrayList<>();

        // --- 1. 罗纹区 ---
        addFixedStep(report, state, "罗纹区", "空转 (橡筋)", "-", "267", "保持", 1.5, "267");
        addFixedStep(report, state, "罗纹区", "加丝罗纹", "-", "267", "保持", 4.0, "267");

        // --- 2. 大身段 ---
        addFixedStep(report, state, "大身放针", "平织段", "-", "267", "保持", 6.0, "267");

        // 6+1+30 逻辑 (先加后织)
        int n30BaseNeedle = state.currentNeedle;
        for (int n = 1; n <= 30; n++) {
            String loopStr = (n == 1) ? "第 01 次" : (n == 30 ? "第 30 次" : "第 N 次");
            String startN = (n == 1) ? String.valueOf(n30BaseNeedle) : (n == 30 ? String.valueOf(n30BaseNeedle + 2 * (n - 1)) : n30BaseNeedle + "+2(N-1)");
            String logic = (n == 1) ? "<strong style='color:red;'>先加针再织</strong>" : "+2 (L1, R1)";
            String endN = (n == 1) ? "269" : (n == 30 ? "327" : "267+2N");
            
            // 物理执行
            int startL = state.currentLine + 1;
            int lines = (int)(6 * 2); 
            state.currentLine += lines;
            state.currentNeedle += 2;
            
            addStep(report, "大身放针", "6+1+30", loopStr, startN, logic, 6.0, startL, state.currentLine, endN);
        }

        // 7+1+10 逻辑 (先织后加)
        int n10BaseNeedle = state.currentNeedle;
        for (int n = 1; n <= 10; n++) {
            String loopStr = (n == 1) ? "第 01 次" : (n == 10 ? "第 10 次" : "...");
            String startN = String.valueOf(state.currentNeedle);
            
            int startL = state.currentLine + 1;
            state.currentLine += (7 * 2);
            state.currentNeedle += 2;
            
            addStep(report, "大身放针", "7+1+10", loopStr, startN, "+2", 7.0, startL, state.currentLine, String.valueOf(state.currentNeedle));
        }

        printTable(report);
    }

    private static void addFixedStep(List<ProcessStep> report, MachineState state, String stage, String ins, String loop, String sN, String log, double c, String eN) {
        int startL = state.currentLine + 1;
        state.currentLine += (int)(c * 2);
        addStep(report, stage, ins, loop, sN, log, c, startL, state.currentLine, eN);
    }

    private static void addStep(List<ProcessStep> report, String stage, String ins, String loop, String sN, String log, double c, int sL, int eL, String eN) {
        ProcessStep p = new ProcessStep();
        p.stage = stage; p.instruction = ins; p.loop = loop; p.startNStr = sN;
        p.logic = log; p.courseUsed = c; p.lineStart = sL; p.lineEnd = eL; p.endNStr = eN;
        report.add(p);
    }

    private static void printTable(List<ProcessStep> report) {
        System.out.println("<table border=\"1\" style=\"border-collapse:collapse; width:100%; text-align:center; font-family:sans-serif;\">");
        System.out.println("<thead style=\"background:#eee;\"><tr><th>工艺阶段</th><th>指令描述</th><th>循环/序号</th><th>起始针数</th><th>变化逻辑</th><th>耗费转数</th><th>软件行号 (2行/转)</th><th>最终针数</th></tr></thead><tbody>");
        
        String lastStage = "";
        for (int i = 0; i < report.size(); i++) {
            ProcessStep curr = report.get(i);
            boolean printStage = !curr.stage.equals(lastStage);
            int span = 0;
            if (printStage) {
                for (ProcessStep p : report) if (p.stage.equals(curr.stage)) span++;
            }
            System.out.println(curr.toHtmlRow(printStage, span));
            lastStage = curr.stage;
        }
        System.out.println("</tbody></table>");
    }
}