package ru.nullpointer.nkbrelation.export;

import java.io.IOException;

import ru.nullpointer.nkbrelation.domain.ExportParams;

/**
 * @author Alexander Yastrebov
 * @author ankostyuk
 */
public interface ReportImageProducer {

    ReportImage getImage(ExportParams params) throws IOException;
}
