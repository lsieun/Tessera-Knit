package lsieun.knit.util;

import lsieun.knit.instruction.*;

import java.util.ArrayList;
import java.util.List;

public class InstructionParser {

    public List<KnitInstruction> parse(String[] instructionArray) {
        List<KnitInstruction> list = new ArrayList<>();
        boolean isFirstComplexAfterPlain = false;

        // 倒序遍历：从数组最后一个元素（底部）开始处理
        for (int i = instructionArray.length - 1; i >= 0; i--) {
            String raw = instructionArray[i].trim();
            KnitInstruction ins = null;

            if (raw.contains("+") || (raw.split("-").length == 3)) {
                // 处理复合指令：如 6+1+30 或 1.5-2-8
                ins = parseComplex(raw, isFirstComplexAfterPlain);
                // 一旦进入复合指令，后续连续的复合指令将不再是“第一个”
                isFirstComplexAfterPlain = false;
            } else if (raw.endsWith("转")) {
                // 处理平织指令：如 6转
                int courses = Integer.parseInt(raw.replace("转", ""));
                ins = new PlainKnitInstruction("P-" + i, raw, courses * 2); // 转换为 Pass
                // 平织之后，标记下一个 Complex 需要先调针
                isFirstComplexAfterPlain = true;
            } else if (raw.endsWith("针")) {
                // 处理平收/平加：如 -20针
                int stitches = Integer.parseInt(raw.replace("针", ""));
                ins = new AdjustOnlyInstruction("A-" + i, raw, stitches);
                // 仅调整不重置 isFirstComplexAfterPlain 标志，保持平织后的相位感
            }

            if (ins != null) {
                list.add(ins);
            }
        }
        return list;
    }

    private KnitInstruction parseComplex(String raw, boolean adjustFirst) {
        // 支持两种格式：6+1+30 (加针) 或 1.5-2-8 (减针)
        String[] parts = raw.contains("+") ? raw.split("\\+") : raw.split("-");
        
        double courses = Double.parseDouble(parts[0]);
        int stitchChange = Integer.parseInt(parts[1]);
        int repeat = Integer.parseInt(parts[2]);
        
        // 如果是减针格式（如 1.5-2-8），解析出的 stitchChange 可能是正数，需转为负数
        if (raw.contains("-") && stitchChange > 0) {
            stitchChange = -stitchChange;
        }

        // 核心转换：转数 -> 趟数 (Pass)
        int intervalPasses = (int) (courses * 2);
        ExecutionOrder order = adjustFirst ? ExecutionOrder.ADJUST_THEN_KNIT : ExecutionOrder.KNIT_THEN_ADJUST;

        return new ComplexInstruction("C-" + raw, raw, intervalPasses, stitchChange, repeat, order);
    }
}