package lsieun.knit.model;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

/**
 * 代表整个织物（Fabric），由多转（Course）纵向堆叠而成
 */
public class KnittingFabric {
    private final String name;
    private final List<KnittingCourse> courses;

    public KnittingFabric(String name) {
        this.name = name;
        this.courses = new ArrayList<>();
    }

    /**
     * 添加一转到织物中
     */
    public void addCourse(KnittingCourse course) {
        this.courses.add(course);
    }

    public String getName() {
        return name;
    }

    /**
     * 获取所有转（不可变列表，保证封装性）
     */
    public List<KnittingCourse> getCourses() {
        return Collections.unmodifiableList(courses);
    }

    /**
     * 获取当前织物的总转数
     */
    public int getTotalCourses() {
        return courses.size();
    }

    @Override
    public String toString() {
        return String.format("织物项目: %s, 总高度: %d 转", name, courses.size());
    }
}