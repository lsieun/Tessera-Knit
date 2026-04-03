package lsieun.knit.engine;

import lsieun.knit.context.InstructionBinding;
import lsieun.knit.context.KnitContext;
import lsieun.knit.instruction.*;
import lsieun.knit.model.KnittingFabric;
import lsieun.knit.model.KnittingPassState;
import lsieun.knit.util.InstructionParser;
import org.junit.jupiter.api.Test;

import java.util.ArrayList;
import java.util.List;

class KnittingEngineTest
{
    @Test
    void test001()
    {
        // 1. 初始化引擎：起始针数 100
        KnittingEngine engine = new KnittingEngine(100);
        System.out.println("=== 引擎启动 ===");
        printState("初始状态", engine.getLastState());

        // 2. 指令 A：平织 2 转 (共 4 趟)
        PlainKnitInstruction plainKnit = new PlainKnitInstruction("INS-001", "平织基座", 2);
        engine.process(plainKnit);
        printState("平织 2 转后", engine.getLastState());

        // 3. 指令 B：复合指令 (6 趟加 1 针，重复 3 次，先织后调)
        // 逻辑：织 6 趟 -> 加 1 针 -> 织 6 趟 -> 加 1 针 -> 织 6 趟 -> 加 1 针
        // 总计增加：3 针；总计行程：18 趟
        ComplexInstruction complex = new ComplexInstruction(
                "INS-002", "袖子加针",
                6, 1, 3, ExecutionOrder.KNIT_THEN_ADJUST
        );
        engine.process(complex);
        printState("复合指令执行后", engine.getLastState());

        // 4. 验证“秩序 (Order)”：检查 KnitContext
        System.out.println("\n=== 秩序检查 (KnitContext) ===");
        KnitContext context = engine.getContext();
        List<KnittingPassState> complexPasses = context.getPassesByInstruction(complex);

        System.out.println("复合指令生成的总行程数: " + complexPasses.size() + " (预期: 18)");

        // 抽取最后一行行程进行深度检查
        KnittingPassState lastPass = complexPasses.get(complexPasses.size() - 1);
        InstructionBinding binding = context.getBinding(lastPass).orElseThrow();

        System.out.println("最后一趟行程详情:");
        System.out.println("  - 循环次数(r): " + binding.repeatIndex());
        System.out.println("  - 行程索引(p): " + binding.passIndexInRepeat());
        System.out.println("  - 物理针数: " + lastPass.stitchCount() + " (预期: 102，因为最后一次加针在编织后)");
        System.out.println("  - 引擎最终逻辑针数: " + engine.getLastState().stitchCount() + " (预期: 103)");
    }

    private static void printState(String label, KnittingPassState state)
    {
        System.out.printf("[%s] -> 转数: %d, 方向: %s, 针数: %d%n",
                label, state.courseIndex(), state.passType(), state.stitchCount());
    }

    @Test
    void testBatchProcess()
    {
        KnittingEngine engine = new KnittingEngine(80);

        // 准备一份简单的工艺单
        List<KnitInstruction> techSheet = List.of(
                new AdjustOnlyInstruction("INS-0", "起针", 20), // 80 -> 100
                new PlainKnitInstruction("INS-1", "罗纹口", 10), // 织 10 转
                new ComplexInstruction("INS-2", "扩身", 4, 2, 5, ExecutionOrder.ADJUST_THEN_KNIT)
        );

        // 一键执行
        engine.processAll(techSheet);

        // 获取并打印结果
        KnittingFabric fabric = engine.getFabric("批量测试织物");
        fabric.printDetailedReport();
    }

    @Test
    void test003()
    {
        // 1. 初始化引擎：假设初始起针数为 80 针
        KnittingEngine engine = new KnittingEngine(267);

        // 2. 将你的工艺描述转换为指令列表
        List<KnitInstruction> techSheet = new ArrayList<>();

        // -- [A] 基础平织段 (6转 = 12趟) --
        techSheet.add(new PlainKnitInstruction("I1", "初始平织", 12));

        // -- [B] 第一个复合指令 (紧跟平织，采用 ADJUST_THEN_KNIT) --
        // 6+1+30: 这里的 6转 对应 12趟
        techSheet.add(new ComplexInstruction("I2", "袖下放针1", 12, 1, 30, ExecutionOrder.ADJUST_THEN_KNIT));

        // -- [C] 后续复合指令 (紧跟复合指令，采用 KNIT_THEN_ADJUST) --
        // 7+1+10: 这里的 7转 对应 14趟
        techSheet.add(new ComplexInstruction("I3", "袖下放针2", 14, 1, 10, ExecutionOrder.KNIT_THEN_ADJUST));

        // -- [D] 过渡平织段 (19转 = 38趟) --
        techSheet.add(new PlainKnitInstruction("I4", "腋下平织", 38));

        // -- [E] 腋下平收 (20针) --
        techSheet.add(new AdjustOnlyInstruction("I5", "腋下平收", -20));

        // -- [F] 绱袖段起始 (1转 = 2趟) --
        techSheet.add(new PlainKnitInstruction("I6", "收针过渡", 2));

        // -- [G] 绱袖收针序列 --
        // 1-2-2: 1转=2趟。紧跟平织 I6，第一个采用 ADJUST_THEN_KNIT
        techSheet.add(new ComplexInstruction("I7", "绱袖收针1", 2, -2, 2, ExecutionOrder.ADJUST_THEN_KNIT));

        // 后续 Complex 采用 KNIT_THEN_ADJUST
        // 1.5-2-8: 1.5转 = 3趟
        techSheet.add(new ComplexInstruction("I8", "绱袖收针2", 3, -2, 8, ExecutionOrder.KNIT_THEN_ADJUST));
        // 2-2-24: 2转 = 4趟
        techSheet.add(new ComplexInstruction("I9", "绱袖收针3", 4, -2, 24, ExecutionOrder.KNIT_THEN_ADJUST));
        // 1.5-2-6: 1.5转 = 3趟
        techSheet.add(new ComplexInstruction("I10", "绱袖收针4", 3, -2, 6, ExecutionOrder.KNIT_THEN_ADJUST));
        // 1-2-4: 1转 = 2趟
        techSheet.add(new ComplexInstruction("I11", "绱袖收针5", 2, -2, 4, ExecutionOrder.KNIT_THEN_ADJUST));

        // 3. 执行生产
        System.out.println("开始执行生产流程...");
        engine.processAll(techSheet);

        // 4. 输出结果
        KnittingFabric fabric = engine.getFabric("高级袖片-Pass统一版");
        System.out.println("\n" + fabric.toString());
        System.out.println("最终针数验证: " + engine.getLastState().stitchCount() + " (预期: 12)");

        // 打印简要报告
        System.out.println("\n关键节点核对：");
        System.out.println("总行程数 (Passes): " + fabric.getCourses().stream()
                .mapToInt(c -> (c.getGoPass().isPresent() ? 1 : 0) + (c.getBackPass().isPresent() ? 1 : 0))
                .sum());
    }

    @Test
    void test004 () {
        // 6转、6+1+30、7+1+10、19转、20针（平收）、1转、1-2-2、1.5-2-8、2-2-24、1.5-2-6、1-2-4
        String[] instructionArray = {
                "1-2-4",
                "1.5-2-6",
                "2-2-24",
                "1.5-2-8",
                "1-2-2",
                "1转",
                "-20针",
                "19转",
                "7+1+10",
                "6+1+30",
                "6转"
        };


        InstructionParser parser = new InstructionParser();
        List<KnitInstruction> techSheet = parser.parse(instructionArray);

        KnittingEngine engine = new KnittingEngine(267);
        engine.processAll(techSheet);

//        KnittingFabric fabric = engine.getFabric("从下往上的袖片");
//        fabric.printDetailedReport();

        engine.printInvertedReport("Hello");
    }
}
