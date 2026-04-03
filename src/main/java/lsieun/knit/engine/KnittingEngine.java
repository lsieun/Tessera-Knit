package lsieun.knit.engine;

import lsieun.knit.context.InstructionBinding;
import lsieun.knit.context.KnitContext;
import lsieun.knit.instruction.*;
import lsieun.knit.model.KnittingCourse;
import lsieun.knit.model.KnittingFabric;
import lsieun.knit.model.KnittingPassState;
import lsieun.knit.model.PassType;

import java.util.*;

public class KnittingEngine {
    private KnittingPassState lastState; // 引擎当前的物理与逻辑状态
    private final KnitContext context;   // 秩序容器：记录 Pass 与 Instruction 的关系

    // 这里定义 history，用于存储引擎运行至今产生的所有物理行程
    private final List<KnittingPassState> history = new ArrayList<>();

    public KnittingEngine(int startStitchCount) {
        // 使用初始状态工厂：Course 0, 机头在左(BACK), 指定针数
        this.lastState = KnittingPassState.initial(startStitchCount);
        this.context = new KnitContext();
    }

    public KnittingFabric getFabric(String fabricName) {
        KnittingFabric fabric = new KnittingFabric(fabricName);

        // 使用 TreeMap 确保转数按 1, 2, 3... 的顺序排列
        Map<Integer, KnittingCourse> courseMap = new TreeMap<>();

        for (KnittingPassState pass : history) {
            // 如果这一转还没创建，先创建它
            KnittingCourse course = courseMap.computeIfAbsent(
                    pass.courseIndex(),
                    KnittingCourse::new
            );

            // 根据类型放入对应的槽位
            if (pass.passType() == PassType.GO) {
                course.setGoPass(pass);
            } else if (pass.passType() == PassType.BACK) {
                course.setBackPass(pass);
            }
        }

        // 将组装好的 Course 依次放入 Fabric 实体
        for (KnittingCourse course : courseMap.values()) {
            fabric.addCourse(course);
        }

        return fabric;
    }

    public void printInvertedReport(String fabricName) {
        System.out.println("================================================================================================================================");
        System.out.printf("| %-15s | %-6s | %-12s | %-8s | %-8s | %-8s | %-20s | %-10s |%n",
                "Instruction", "Course", "Direction", "Before", "Pass", "After", "Action", "Repeat");
        System.out.println("--------------------------------------------------------------------------------------------------------------------------------");

        KnittingFabric fabric = getFabric(fabricName);

        // 1. 获取所有 Course 并倒序遍历 (让最后一转在最上面)
        List<KnittingCourse> allCourses = fabric.getCourses();
        for (int i = allCourses.size() - 1; i >= 0; i--) {
            KnittingCourse course = allCourses.get(i);

            // 每一转包含两个 Pass：先打印 BACK (回程)，再打印 GO (去程)
            // 这样在视觉上：GO 在下，BACK 在上，符合一个 Course 的堆叠感
            printPassRow(course.getBackPass(), course.getCourseIndex());
            printPassRow(course.getGoPass(), course.getCourseIndex());

            if (i > 0) System.out.println("|-----------------|--------|--------------|----------|----------|----------|----------------------|------------|");
        }
        System.out.println("================================================================================================================================");
    }

    private void printPassRow(Optional<KnittingPassState> passOpt, int courseIdx) {
        if (passOpt.isEmpty()) return;
        KnittingPassState pass = passOpt.get();

        // 从 Context 获取指令绑定信息
        var binding = context.getBinding(pass).orElse(null);
        String insName = (binding != null) ? binding.instruction().getName() : "";
        String repeatInfo = (binding != null) ? String.format("R%d-P%d", binding.repeatIndex(), binding.passIndexInRepeat()) : "-";

        // 获取动作语义
        String action = determineActionLabel(binding);

        // 格式化输出
        // 注意：Before/After 逻辑需要根据 Action 调整，这里简化展示当前 Pass 的针数
        System.out.printf("| %-15s | C%-5d | %-12s | %-8d | %-8d | %-8d | %-20s | %-10s |%n",
                insName,
                courseIdx,
                pass.passType() == PassType.GO ? "GO (--->)" : "BACK (<---)",
                pass.stitchCount(), // Before (简化示例，实际需取上一状态)
                pass.stitchCount(), // Pass Stitches
                pass.stitchCount(), // After
                action,
                repeatInfo);
    }

    private String determineActionLabel(InstructionBinding binding) {
        if (binding == null) return "KNIT";
        KnitInstruction ins = binding.instruction();

        if (ins instanceof PlainKnitInstruction) return "KNIT";
        if (ins instanceof AdjustOnlyInstruction) return "ADJUST";
        if (ins instanceof ComplexInstruction c) {
            return c.getOrder().toString(); // ADJUST_THEN_KNIT 等
        }
        return "KNIT";
    }

    /**
     * 批量处理指令列表（模拟工艺单执行）
     * @param instructions 指令集合
     */
    public void processAll(List<KnitInstruction> instructions) {
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
    public void process(KnitInstruction ins) {
        Objects.requireNonNull(ins, "指令不能为空");

        // 使用模式匹配 switch (Java 17+) 使集中处理更加优雅
        switch (ins) {
            case PlainKnitInstruction p  -> handlePlainKnit(p);
            case AdjustOnlyInstruction a -> handleAdjustOnly(a);
            case ComplexInstruction c    -> handleComplex(c);
            default -> throw new IllegalArgumentException("未知的指令类型: " + ins.getClass());
        }
    }

    // --- 内部逻辑处理器 (集中化实现) ---

    private void handlePlainKnit(PlainKnitInstruction ins) {
        // 平织：仅生成物理行程，针数不变
        for (int p = 0; p < ins.getTotalPasses(); p++) {
            generateAndRecordPass(ins, 1, p); // 平织通常视为 1 次重复
        }
    }

    private void handleAdjustOnly(AdjustOnlyInstruction ins) {
        // 仅调整：不生成物理行程，仅更新逻辑状态指针
        this.lastState = lastState.mutate()
                .stitchCount(lastState.stitchCount() + ins.getStitchChange())
                .build();
    }

    private void handleComplex(ComplexInstruction ins) {
        for (int r = 1; r <= ins.getRepeatTimes(); r++) {
            // 1. 先调针
            if (ins.getOrder() == ExecutionOrder.ADJUST_THEN_KNIT) {
                applyStitchChange(ins.getStitchChange());
                if (r == ins.getRepeatTimes()) break; // 最后一次只调针不编织
            }

            // 2. 编织行程
            for (int p = 0; p < ins.getIntervalPasses(); p++) {
                generateAndRecordPass(ins, r, p);
            }

            // 3. 后调针
            if (ins.getOrder() == ExecutionOrder.KNIT_THEN_ADJUST) {
                applyStitchChange(ins.getStitchChange());
            }
        }
    }

    // --- 原子操作 API (支撑显式语义) ---

    /**
     * 物理动作：推导下一个 Pass 并登记造册
     */
    private void generateAndRecordPass(KnitInstruction ins, int repeatIndex, int passIndexInRepeat) {
        // 1. 逻辑推导
        PassType nextType = lastState.passType().next();
        int nextCourse = (nextType == PassType.GO) ? lastState.courseIndex() + 1 : lastState.courseIndex();

        KnittingPassState currentState = lastState.mutate()
                .courseIndex(nextCourse)
                .passType(nextType)
                .stitchCount(lastState.stitchCount())
                .build();

        // 2. 建立秩序 (Context)
        context.bind(currentState, new InstructionBinding(ins, repeatIndex, passIndexInRepeat));

        // 3. 记录物理轨迹 (关键点！)
        this.history.add(currentState);

        // 4. 更新指针
        this.lastState = currentState;
    }

    /**
     * 逻辑动作：仅更新针数
     */
    private void applyStitchChange(int change) {
        this.lastState = lastState.mutate()
                .stitchCount(lastState.stitchCount() + change)
                .build();
    }

    // --- Getters ---
    public KnittingPassState getLastState() { return lastState; }
    public KnitContext getContext() { return context; }
}