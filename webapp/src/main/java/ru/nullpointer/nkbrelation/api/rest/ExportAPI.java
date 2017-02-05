package ru.nullpointer.nkbrelation.api.rest;

import ru.nullpointer.nkbrelation.domain.ExportParams;
import java.io.IOException;
import javax.annotation.Resource;
import javax.servlet.http.Cookie;
import javax.servlet.http.HttpServletResponse;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.WebDataBinder;
import org.springframework.web.bind.annotation.InitBinder;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import ru.nullpointer.nkbrelation.api.log.APILogEntry;
import ru.nullpointer.nkbrelation.service.ExportService;
import ru.nullpointer.nkbrelation.service.BadArgumentException;

/**
 *
 * @author ankostyuk
 * @author Alexander Yastrebov
 */
@Controller
public class ExportAPI extends AbstractAPI {

    private static final String FILE_DOWNLOAD_COOKIE_NAME = "fileDownload";
    private static final String FILE_DOWNLOAD_COOKIE_VALUE = "true";
    private static final String FILE_DOWNLOAD_COOKIE_PATH = "/";
    //
    @Resource
    private ExportService exportService;

    @InitBinder("exportParams")
    public void initBinder(WebDataBinder binder) {
        binder.setDisallowedFields("format");
    }

    @RequestMapping(value = "/api/export/{format}/{fileName}", method = RequestMethod.POST)
    public void export(@PathVariable("format") String fmt, ExportParams params,
            HttpServletResponse response, APILogEntry entry) throws IOException {

        ExportParams.Format format = getFormat(fmt);

        setFileDownloadResponseHeaders(response, format);

        params.setFormat(format);

        exportService.export(params, response.getOutputStream());

        logger.info("{}, report: {}, type: {}, scale: {}, page: {}", entry, params.getReportId(), params.getFormat(), params.getScale(), params.getPageSize());
    }

    private ExportParams.Format getFormat(String fmt) {
        try {
            return ExportParams.Format.valueOf(fmt.toUpperCase());
        } catch (IllegalArgumentException ex) {
            throw new BadArgumentException("Unsupported format " + fmt);
        }
    }

    public static void setFileDownloadResponseHeaders(HttpServletResponse response, ExportParams.Format format) {
        response.setHeader("Content-Disposition", "attachment");
        response.setContentType(format.getMime());

        Cookie cookie = new Cookie(FILE_DOWNLOAD_COOKIE_NAME, FILE_DOWNLOAD_COOKIE_VALUE);
        cookie.setPath(FILE_DOWNLOAD_COOKIE_PATH);
        response.addCookie(cookie);
    }
}
