package ru.nullpointer.nkbrelation.web.internal;

import java.io.IOException;
import java.util.List;

import javax.annotation.Resource;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.springframework.stereotype.Controller;
import org.springframework.ui.ModelMap;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;

import ru.nullpointer.nkbrelation.domain.meta.NodeType;
import ru.nullpointer.nkbrelation.domain.meta.RelationType;
import ru.nullpointer.nkbrelation.domain.report.Report;
import ru.nullpointer.nkbrelation.service.GraphService;
import ru.nullpointer.nkbrelation.service.ReportService;

/**
 * @author Alexander Yastrebov
 * @author ankostyuk
 */
@Controller
public class ReportExportController {

    @Resource
    private ReportService reportService;
    @Resource
    private GraphService graphService;
    @Resource
    private ObjectMapper objectMapper;

    @RequestMapping(value = "/internal/export/report/{reportId}", method = RequestMethod.GET)
    public String getReport(
            @PathVariable("reportId") String reportId,
            ModelMap model) throws IOException {

        Report report = reportService.getReportInternal(reportId);
        model.put("report", report);
        model.put("report_json", objectMapper.writeValueAsString(report));

        List<NodeType> nodeTypes = graphService.getNodeTypeList();
        model.put("node_types_json", objectMapper.writeValueAsString(nodeTypes));

        List<RelationType> relationTypes = graphService.getRelationTypeList();
        model.put("relation_types_json", objectMapper.writeValueAsString(relationTypes));

        return "views/internal/export/report";
    }
}
