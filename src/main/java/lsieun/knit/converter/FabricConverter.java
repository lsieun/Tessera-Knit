package lsieun.knit.converter;

import lsieun.knit.event.KnitEvent;
import lsieun.knit.event.KnitEventConverter;
import lsieun.knit.model.KnitCourse;
import lsieun.knit.model.KnitFabric;
import lsieun.knit.model.KnitPass;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.TreeMap;
import java.util.stream.Collectors;

/**
 * 专门负责从事件流中“缝合”出物理织物模型的转换器
 */
public class FabricConverter implements KnitEventConverter<KnitFabric>
{
    private final String fabricName;

    public FabricConverter(String fabricName)
    {
        this.fabricName = fabricName;
    }

    @Override
    public KnitFabric convert(List<KnitEvent> events)
    {
        // 提取物理行程
        List<KnitPass> physicalPasses = events.stream()
                .filter(KnitEvent::isPhysical)
                .map(e -> e.pass().orElseThrow())
                .toList();

        // 按 Course 索引重新组织
        Map<Integer, List<KnitPass>> courseGroups = physicalPasses.stream()
                .collect(Collectors.groupingBy(
                        KnitPass::courseIndex,
                        TreeMap::new,
                        Collectors.toList()
                ));

        List<KnitCourse> courses = new ArrayList<>();
        courseGroups.forEach((index, passes) -> {
            KnitCourse course = new KnitCourse(index);
            passes.forEach(course::addPass);
            courses.add(course);
        });

        return new KnitFabric(fabricName, courses);
    }
}