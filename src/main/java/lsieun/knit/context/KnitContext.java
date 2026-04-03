package lsieun.knit.context;

import lsieun.knit.instruction.KnitInstruction;
import lsieun.knit.model.KnitPass;

import java.util.*;

public class KnitContext {
    // 1. 通过物理行程找身份 (Pass -> Binding)
    private final Map<KnitPass, InstructionBinding> passToBinding = new HashMap<>();

    // 2. 通过指令找所有相关的物理行程 (Instruction -> List<Pass>)
    private final Map<KnitInstruction, List<KnitPass>> instructionToPasses = new HashMap<>();

    /**
     * 建立绑定关系
     */
    public void bind(KnitPass pass, InstructionBinding binding) {
        passToBinding.put(pass, binding);

        instructionToPasses
                .computeIfAbsent(binding.instruction(), k -> new ArrayList<>())
                .add(pass);
    }

    /**
     * 查询：通过行程获取其身世
     */
    public Optional<InstructionBinding> getBinding(KnitPass pass) {
        return Optional.ofNullable(passToBinding.get(pass));
    }

    /**
     * 查询：通过指令获取其产生的所有行程
     */
    public List<KnitPass> getPassesByInstruction(KnitInstruction ins) {
        return instructionToPasses.getOrDefault(ins, Collections.emptyList());
    }
}