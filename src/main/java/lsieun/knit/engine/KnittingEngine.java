package lsieun.knit.engine;

import lsieun.knit.context.KnitContext;
import lsieun.knit.model.*;
import lsieun.knit.instruction.*;
import java.util.List;

public class KnittingEngine {
    private KnittingPassState lastState;
    private final KnitContext context;

    public KnittingEngine(KnittingPassState lastState, KnitContext context)
    {
        this.lastState = lastState;
        this.context = context;
    }

    public void process(KnitInstruction ins) {

    }

    private void savePass(KnittingPassState p, KnitInstruction ins) {
        // 1. 放入 Fabric
        // 2. 在 Context 中建立绑定关系
    }
}