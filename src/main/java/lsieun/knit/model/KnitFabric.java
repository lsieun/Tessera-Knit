package lsieun.knit.model;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

/**
 * 代表整个织物（Fabric），由多转（Course）纵向堆叠而成
 */
public record KnitFabric(String name, List<KnitCourse> courses)
{
    public KnitFabric(String name)
    {
        this(name, new ArrayList<>());
    }

    /**
     * 添加一转到织物中
     */
    public void addCourse(KnitCourse course)
    {
        this.courses.add(course);
    }

    /**
     * 获取所有转（不可变列表，保证封装性）
     */
    @Override
    public List<KnitCourse> courses()
    {
        return Collections.unmodifiableList(courses);
    }

    /**
     * 获取当前织物的总转数
     */
    public int getTotalCourses()
    {
        return courses.size();
    }

    @Override
    public String toString()
    {
        return String.format("织物项目: %s, 总高度: %d 转", name, courses.size());
    }

    public void printDetailedReport()
    {
        System.out.println("织物报告: " + name);
        System.out.println("----------------------------------------------------------------");
        System.out.printf("%-10s | %-12s | %-12s | %-10s%n", "COURSE", "GO_STITCHES", "BACK_STITCHES", "STATUS");
        System.out.println("----------------------------------------------------------------");

        for (KnitCourse c : courses) {
            String goStr = c.getGoPass().map(p -> String.valueOf(p.stitchCount())).orElse("-");
            String backStr = c.getBackPass().map(p -> String.valueOf(p.stitchCount())).orElse("-");
            String status = c.isCompleted() ? "DONE" : "INCOMPLETE";

            System.out.printf("C-%-8d | %-12s | %-12s | %-10s%n",
                    c.getCourseIndex(), goStr, backStr, status);
        }
        System.out.println("----------------------------------------------------------------");
    }


}