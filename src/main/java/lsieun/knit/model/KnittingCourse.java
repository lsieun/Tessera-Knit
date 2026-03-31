package lsieun.knit.model;

import java.util.Optional;

/**
 * 代表编织中的“一转”（Course）。
 * 支持半转状态（只有 goPass），并处理去程与回程针数不一致的情况。
 */
public class KnittingCourse {
    private final int courseIndex;
    private KnittingPassState goPass;
    private KnittingPassState backPass;

    public KnittingCourse(int courseIndex) {
        this.courseIndex = courseIndex;
    }

    // 设置去程
    public void setGoPass(KnittingPassState goPass) {
        if (goPass != null && goPass.passType() != PassType.GO) {
            throw new IllegalArgumentException("必须是 GO 类型的行程");
        }
        this.goPass = goPass;
    }

    // 设置回程
    public void setBackPass(KnittingPassState backPass) {
        if (backPass != null && backPass.passType() != PassType.BACK) {
            throw new IllegalArgumentException("必须是 BACK 类型的行程");
        }
        this.backPass = backPass;
    }

    /**
     * 检查这一转是否已经编织完成（往返都已执行）
     */
    public boolean isCompleted() {
        return goPass != null && backPass != null;
    }

    public int getCourseIndex() {
        return courseIndex;
    }

    public Optional<KnittingPassState> getGoPass() {
        return Optional.ofNullable(goPass);
    }

    public Optional<KnittingPassState> getBackPass() {
        return Optional.ofNullable(backPass);
    }

    /**
     * 获取这一转涉及的最大针数宽度。
     * 如果两程不一致，取其最大值，这代表了机器机头在这一转中覆盖的最大物理范围。
     */
    public int getMaxStitchWidth() {
        int goWidth = (goPass != null) ? goPass.stitchCount() : 0;
        int backWidth = (backPass != null) ? backPass.stitchCount() : 0;
        return Math.max(goWidth, backWidth);
    }
}