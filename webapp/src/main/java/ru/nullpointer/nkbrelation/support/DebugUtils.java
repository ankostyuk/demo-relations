package ru.nullpointer.nkbrelation.support;

import org.apache.commons.collections.MapUtils;
import ru.nullpointer.nkbrelation.domain.NodeId;

import java.util.List;
import java.util.Map;

/**
 * @author ankostyuk
 */
public final class DebugUtils {

    private DebugUtils() {
    }

    public static void debugNode(Map<String, Object> node, boolean debugRelations) {
        MapUtils.debugPrint(System.out, "node", node);
        if (debugRelations) {
            debugNodeRelations(node);
        }
    }

    public static void debugNodeRelations(Map<String, Object> node) {
        debugMapList((List<Map<String, Object>>) node.get("_relations"), "node relations", "relation");
    }

    public static void debugMapList(List<Map<String, Object>> list, String listLabel, String itemLabel) {
        if (list == null) {
            return;
        }

        System.out.println((new StringBuilder("\n"))
            .append(listLabel)
            .append(": ")
            .append(list.size())
            .append("...")
            .toString()
        );

        for (int i = 0; i < list.size(); i++) {
            Map<String, Object> map = list.get(i);

            MapUtils.debugPrint(System.out,
                (new StringBuilder("\n"))
                    .append(itemLabel)
                    .append(" ")
                    .append(i + 1)
                    .toString(),
                map
            );
        }
    }
}
