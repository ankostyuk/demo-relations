package ru.nullpointer.nkbrelation.service;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import ru.nullpointer.nkbrelation.domain.meta.RelationType;

import javax.annotation.Resource;

/**
 * @author ankostyuk
 */
@Service
public class HistoryService {

    private static Logger logger = LoggerFactory.getLogger(HistoryService.class);
    //
    @Resource
    private GraphService graphService;
    //

    public boolean isHistoryRelationType(String relationTypeName) {
        RelationType relationType = graphService.getRelationTypeByName(relationTypeName);
        return relationType.isHistorySupport();
    }
}
