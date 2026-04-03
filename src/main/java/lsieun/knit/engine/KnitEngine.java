package lsieun.knit.engine;

import lsieun.knit.context.InstructionBinding;
import lsieun.knit.context.KnitContext;
import lsieun.knit.event.KnitEvent;
import lsieun.knit.instruction.*;
import lsieun.knit.model.KnitPass;
import lsieun.knit.model.PassType;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import java.util.Optional;

public class KnitEngine
{
    private final KnitPass initialState; // 记录原点
    private KnitPass lastState;          // 当前指针
    private final KnitContext context;   // 秩序容器：记录 Pass 与 Instruction 的关系

    // 核心仓库：记录所有事件（编织+调针）
    private final List<KnitEvent> eventLog = new ArrayList<>();

    public KnitEngine(int startStitchCount)
    {
        // 1. 定义初始状态：Course 0, 机头在左(BACK), 指定针数
        this.initialState = KnitPass.initial(startStitchCount);
        this.lastState = this.initialState;
        this.context = new KnitContext();

        // 2. 将初始状态作为“序幕事件”记录（可选）
        // 这样你的数据仓库里第一条数据就是原点状态
        this.eventLog.add(new KnitEvent(
                new PlainKnitInstruction("START", "Initial State", 0), // 虚拟指令
                new InstructionBinding(null, 0, 0),
                startStitchCount,
                startStitchCount,
                Optional.of(initialState)
        ));
    }

    public KnitPass getInitialState()
    {
        return initialState;
    }

    public List<KnitEvent> getEventLog()
    {
        return eventLog;
    }

//    public void printInvertedReport(String fabricName)
//    {
//        System.out.println("================================================================================================================================");
//        System.out.printf("| %-15s | %-6s | %-12s | %-8s | %-8s | %-8s | %-20s | %-10s |%n",
//                "Instruction", "Course", "Direction", "Before", "Pass", "After", "Action", "Repeat");
//        System.out.println("--------------------------------------------------------------------------------------------------------------------------------");
//
//        KnitFabric fabric = getFabric(fabricName);
//
//        // 1. 获取所有 Course 并倒序遍历 (让最后一转在最上面)
//        List<KnitCourse> allCourses = fabric.getCourses();
//        for (int i = allCourses.size() - 1; i >= 0; i--) {
//            KnitCourse course = allCourses.get(i);
//
//            // 每一转包含两个 Pass：先打印 BACK (回程)，再打印 GO (去程)
//            // 这样在视觉上：GO 在下，BACK 在上，符合一个 Course 的堆叠感
//            printPassRow(course.getBackPass(), course.getCourseIndex());
//            printPassRow(course.getGoPass(), course.getCourseIndex());
//
//            if (i > 0)
//                System.out.println("|-----------------|--------|--------------|----------|----------|----------|----------------------|------------|");
//        }
//        System.out.println("================================================================================================================================");
//    }

//    private void printPassRow(Optional<KnitPass> passOpt, int courseIdx)
//    {
//        if (passOpt.isEmpty()) return;
//        KnitPass pass = passOpt.get();
//
//        // 从 Context 获取指令绑定信息
//        var binding = context.getBinding(pass).orElse(null);
//        String insName = (binding != null) ? binding.instruction().getName() : "";
//        String repeatInfo = (binding != null) ? String.format("R%d-P%d", binding.repeatIndex(), binding.passIndexInRepeat()) : "-";
//
//        // 获取动作语义
//        String action = determineActionLabel(binding);
//
//        // 格式化输出
//        // 注意：Before/After 逻辑需要根据 Action 调整，这里简化展示当前 Pass 的针数
//        System.out.printf("| %-15s | C%-5d | %-12s | %-8d | %-8d | %-8d | %-20s | %-10s |%n",
//                insName,
//                courseIdx,
//                pass.passType() == PassType.GO ? "GO (--->)" : "BACK (<---)",
//                pass.stitchCount(), // Before (简化示例，实际需取上一状态)
//                pass.stitchCount(), // Pass Stitches
//                pass.stitchCount(), // After
//                action,
//                repeatInfo);
//    }

//    private String determineActionLabel(InstructionBinding binding)
//    {
//        if (binding == null) return "KNIT";
//        KnitInstruction ins = binding.instruction();
//
//        if (ins instanceof PlainKnitInstruction) return "KNIT";
//        if (ins instanceof AdjustOnlyInstruction) return "ADJUST";
//        if (ins instanceof ComplexInstruction c) {
//            return c.getOrder().toString(); // ADJUST_THEN_KNIT 等
//        }
//        return "KNIT";
//    }

    /**
     * 批量处理指令列表（模拟工艺单执行）
     *
     * @param instructions 指令集合
     */
    public void processAll(List<KnitInstruction> instructions)
    {
        if (instructions == null || instructions.isEmpty()) {
            return;
        }

        // 依次调用单条指令处理逻辑
        // 引擎的 lastState 会在每条指令执行后自动衔接到下一条
        System.out.println(">>> 开始批量生产，总计指令数: " + instructions.size());
        for (int i = 0; i < instructions.size(); i++) {
            KnitInstruction ins = instructions.get(i);
            String info = String.format("即将处理第|%03d|指令：%s", i, ins);
            System.out.println(info);
            // 可以在这里添加回调或进度上报
            this.process(ins);
        }
        System.out.println("<<< 批量生产完成。");
    }

    /**
     * 核心调度方法：根据指令类型分发处理逻辑
     */
    public void process(KnitInstruction ins)
    {
        Objects.requireNonNull(ins, "指令不能为空");

        // 使用模式匹配 switch (Java 17+) 使集中处理更加优雅
        switch (ins) {
            case PlainKnitInstruction p -> handlePlainKnit(p);
            case AdjustOnlyInstruction a -> handleAdjustOnly(a);
            case ComplexInstruction c -> handleComplex(c);
            default -> throw new IllegalArgumentException("未知的指令类型: " + ins.getClass());
        }
    }

    // --- 内部逻辑处理器 (集中化实现) ---

    private void handlePlainKnit(PlainKnitInstruction ins)
    {
        // 平织：仅生成物理行程，针数不变
        // 平织通常视为 1 次重复，根据指令要求的总行程数进行循环
        for (int p = 0; p < ins.getTotalPasses(); p++) {
            // 逻辑位置固定为 R1 (第1次重复)，p 为当前行程索引
            generateAndRecordPass(ins, 1, p);
        }
    }

    private void handleAdjustOnly(AdjustOnlyInstruction ins)
    {
        // 仅调整：不生成物理行程，仅更新逻辑状态指针
//        this.lastState = lastState.mutate()
//                .stitchCount(lastState.stitchCount() + ins.getStitchChange())
//                .build();

        // 调用 applyStitchChange，将逻辑动作记录到 eventLog 中
        // 对于纯调针指令，repeatIndex 和 passIndex 默认为 1 和 0
        applyStitchChange(ins, 1, 0, ins.getStitchChange());
    }

    private void handleComplex(ComplexInstruction ins)
    {
//        for (int r = 1; r <= ins.getRepeatTimes(); r++) {
//            // 1. 先调针
//            if (ins.getOrder() == ExecutionOrder.ADJUST_THEN_KNIT) {
//                applyStitchChange(ins.getStitchChange());
//                if (r == ins.getRepeatTimes()) break; // 最后一次只调针不编织
//            }
//
//            // 2. 编织行程
//            for (int p = 0; p < ins.getIntervalPasses(); p++) {
//                generateAndRecordPass(ins, r, p);
//            }
//
//            // 3. 后调针
//            if (ins.getOrder() == ExecutionOrder.KNIT_THEN_ADJUST) {
//                applyStitchChange(ins.getStitchChange());
//            }
//        }

        for (int r = 1; r <= ins.getRepeatTimes(); r++) {

            // 1. 先调针相位 (ADJUST_THEN_KNIT)
            if (ins.getOrder() == ExecutionOrder.ADJUST_THEN_KNIT) {
                applyStitchChange(ins, r, 0, ins.getStitchChange());

                // 关键逻辑：如果是最后一次循环，完成调针后直接跳出，不再进行后续编织
                if (r == ins.getRepeatTimes()) {
                    break;
                }
            }

            // 2. 编织行程相位
            for (int p = 0; p < ins.getIntervalPasses(); p++) {
                generateAndRecordPass(ins, r, p);
            }

            // 3. 后调针相位 (KNIT_THEN_ADJUST)
            if (ins.getOrder() == ExecutionOrder.KNIT_THEN_ADJUST) {
                applyStitchChange(ins, r, 0, ins.getStitchChange());
            }
        }
    }

    // --- 原子操作 API (支撑显式语义) ---

    /**
     * 物理动作：生成物理行程并记录事件
     */
    private void generateAndRecordPass(KnitInstruction ins, int repeatIndex, int passIndexInRepeat)
    {
        int before = lastState.stitchCount();

        // 1. 执行物理推导（原有逻辑）
        PassType nextType = lastState.passType().next();
        int nextCourse = (nextType == PassType.GO) ? lastState.courseIndex() + 1 : lastState.courseIndex();
        KnitPass currentState = lastState.mutate()
                .courseIndex(nextCourse)
                .passType(nextType)
                .stitchCount(before)
                .build();

        // 2. 建立秩序 (Context)
        InstructionBinding binding = new InstructionBinding(ins, repeatIndex, passIndexInRepeat);
        context.bind(currentState, binding);

        // 3. 记录到数据中心
        eventLog.add(new KnitEvent(ins, binding, before, before, Optional.of(currentState)));

        // 4. 更新指针
        this.lastState = currentState;
    }

    /**
     * 逻辑动作：记录调针事件
     */
    private void applyStitchChange(KnitInstruction ins, int repeatIndex, int passIndexInRepeat, int change)
    {
        int before = lastState.stitchCount();
        int after = before + change * 2;

        InstructionBinding binding = new InstructionBinding(ins, repeatIndex, passIndexInRepeat);

        // 记录调针事件（无 Pass）
        eventLog.add(new KnitEvent(ins, binding, before, after, Optional.empty()));

        this.lastState = lastState.mutate().stitchCount(after).build();
    }

    /**
     * 物理动作：推导下一个 Pass 并登记造册
     */
//    private void generateAndRecordPass(KnitInstruction ins, int repeatIndex, int passIndexInRepeat) {
//        // 1. 逻辑推导
//        PassType nextType = lastState.passType().next();
//        int nextCourse = (nextType == PassType.GO) ? lastState.courseIndex() + 1 : lastState.courseIndex();
//
//        KnitPass currentState = lastState.mutate()
//                .courseIndex(nextCourse)
//                .passType(nextType)
//                .stitchCount(lastState.stitchCount())
//                .build();
//
//        // 2. 建立秩序 (Context)
//        context.bind(currentState, new InstructionBinding(ins, repeatIndex, passIndexInRepeat));
//
//        // 3. 记录物理轨迹 (关键点！)
//        this.history.add(currentState);
//
//        // 4. 更新指针
//        this.lastState = currentState;
//    }

    /**
     * 逻辑动作：仅更新针数
     */
//    private void applyStitchChange(int change) {
//        this.lastState = lastState.mutate()
//                .stitchCount(lastState.stitchCount() + change)
//                .build();
//    }

    // --- Getters ---
    public KnitPass getLastState()
    {
        return lastState;
    }

    public KnitContext getContext()
    {
        return context;
    }
}