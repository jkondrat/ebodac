package org.motechproject.ebodac.web;

import org.apache.commons.lang.StringUtils;
import org.codehaus.jackson.map.ObjectMapper;
import org.codehaus.jackson.type.TypeReference;
import org.joda.time.DateTime;
import org.joda.time.format.DateTimeFormat;
import org.joda.time.format.DateTimeFormatter;
import org.motechproject.ebodac.constants.EbodacConstants;
import org.motechproject.ebodac.domain.Config;
import org.motechproject.ebodac.domain.MissedVisitsReportDto;
import org.motechproject.ebodac.domain.ReportBoosterVaccination;
import org.motechproject.ebodac.domain.ReportPrimerVaccination;
import org.motechproject.ebodac.domain.Visit;
import org.motechproject.ebodac.exception.EbodacExportException;
import org.motechproject.ebodac.exception.EbodacLookupException;
import org.motechproject.ebodac.service.ConfigService;
import org.motechproject.ebodac.service.ExportService;
import org.motechproject.ebodac.service.LookupService;
import org.motechproject.ebodac.service.impl.csv.SubjectCsvImportCustomizer;
import org.motechproject.ebodac.service.impl.csv.VisitCsvExportCustomizer;
import org.motechproject.ebodac.util.DtoLookupHelper;
import org.motechproject.ebodac.util.PdfBasicTemplate;
import org.motechproject.ebodac.util.PdfReportATemplate;
import org.motechproject.ebodac.util.PdfReportBTemplate;
import org.motechproject.ebodac.util.XlsBasicTemplate;
import org.motechproject.ebodac.util.XlsReportATemplate;
import org.motechproject.ebodac.util.XlsReportBTemplate;
import org.motechproject.ebodac.web.domain.GridSettings;
import org.motechproject.mds.dto.CsvImportResults;
import org.motechproject.mds.dto.EntityDto;
import org.motechproject.mds.ex.csv.CsvImportException;
import org.motechproject.mds.query.QueryParams;
import org.motechproject.mds.service.CsvImportExportService;
import org.motechproject.mds.service.EntityService;
import org.motechproject.mds.util.Constants;
import org.motechproject.mds.util.Order;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.multipart.MultipartFile;

import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.Reader;
import java.util.HashMap;
import java.util.Map;

import static org.apache.commons.lang.CharEncoding.UTF_8;

@Controller
public class InstanceController {
    private static final Logger LOGGER = LoggerFactory.getLogger(InstanceController.class);

    @Autowired
    private LookupService lookupService;

    @Autowired
    private CsvImportExportService csvImportExportService;

    @Autowired
    private SubjectCsvImportCustomizer subjectCsvImportCustomizer;

    @Autowired
    private VisitCsvExportCustomizer visitCsvExportCustomizer;

    @Autowired
    private ExportService exportService;

    @Autowired
    private EntityService entityService;

    @Autowired
    private ConfigService configService;

    private ObjectMapper objectMapper = new ObjectMapper();

    @RequestMapping(value = "/instances/{entityId}/Participantcsvimport", method = RequestMethod.POST)
    @PreAuthorize("hasAnyRole('mdsDataAccess', 'manageSubjects')")
    @ResponseBody
    public long subjectImportCsv(@PathVariable long entityId, @RequestParam(required = true) MultipartFile csvFile) {
        return importCsv(entityId, csvFile);
    }

    @RequestMapping(value = "/instances/{entityId}/Visitcsvimport", method = RequestMethod.POST)
    @PreAuthorize("hasAnyRole('mdsDataAccess', 'manageEbodac')")
    @ResponseBody
    public long visitImportCsv(@PathVariable long entityId, @RequestParam(required = true) MultipartFile csvFile) {
        return importCsv(entityId, csvFile);
    }

    @RequestMapping(value = "/entities/{entityId}/exportInstances", method = RequestMethod.GET)
    public void exportEntityInstances(@PathVariable Long entityId, GridSettings settings,
                                      @RequestParam String exportRecords,
                                      @RequestParam String outputFormat,
                                      HttpServletResponse response) throws IOException {

        EntityDto entityDto = entityService.getEntity(entityId);
        String className = entityDto.getClassName();
        String entityName = entityDto.getName();
        DateTimeFormatter dateTimeFormatter = DateTimeFormat.forPattern("yyyyMMddHHmmss");

        final String fileName = entityName + "_" + DateTime.now().toString(dateTimeFormatter);

        if (Constants.ExportFormat.PDF.equals(outputFormat)) {
            response.setContentType("application/pdf");
        } else {
            response.setContentType("text/csv");
        }
        response.setCharacterEncoding(UTF_8);
        response.setHeader(
                "Content-Disposition",
                "attachment; filename=" + fileName + "." + outputFormat.toLowerCase());

        Order order = StringUtils.isNotEmpty(settings.getSortColumn()) ? new Order(settings.getSortColumn(), settings.getSortDirection()) : null;
        QueryParams queryParams = new QueryParams(1, StringUtils.equalsIgnoreCase(exportRecords, "all") ? null : Integer.valueOf(exportRecords), order);

        if (className.equals(ReportPrimerVaccination.class.getName())) {
            exportEntity(settings, exportRecords, outputFormat, response, "PrimerVaccinationReport",
                    null, ReportPrimerVaccination.class, EbodacConstants.PRIMER_VACCINATION_REPORT_MAP);
        } else if (className.equals(ReportBoosterVaccination.class.getName())) {
            exportEntity(settings, exportRecords, outputFormat, response, "BoosterVaccinationReport",
                    null, ReportBoosterVaccination.class, EbodacConstants.BOOSTER_VACCINATION_REPORT_MAP);
        } else if (Constants.ExportFormat.PDF.equals(outputFormat)) {
            response.setContentType("application/pdf");

            if(className.equals(Visit.class.getName())) {
                csvImportExportService.exportPdf(entityId, response.getOutputStream(), settings.getLookup(), queryParams,
                        settings.getSelectedFields(), getFields(settings), visitCsvExportCustomizer);
            } else {
                csvImportExportService.exportPdf(entityId, response.getOutputStream(), settings.getLookup(), queryParams,
                        settings.getSelectedFields(), getFields(settings));
            }
        } else if (EbodacConstants.CSV_EXPORT_FORMAT.equals(outputFormat)) {
            response.setContentType("text/csv");

            if(className.equals(Visit.class.getName())) {
                csvImportExportService.exportCsv(entityId, response.getWriter(), settings.getLookup(), queryParams,
                        settings.getSelectedFields(), getFields(settings), visitCsvExportCustomizer);
            } else {
                csvImportExportService.exportCsv(entityId, response.getWriter(), settings.getLookup(), queryParams,
                        settings.getSelectedFields(), getFields(settings));
            }
        } else {
            throw new IllegalArgumentException("Invalid export format: " + outputFormat);
        }
    }

    private long importCsv(long entityId, MultipartFile csvFile) {
        try {
            try (InputStream in = csvFile.getInputStream()) {
                Reader reader = new InputStreamReader(in);
                CsvImportResults results = csvImportExportService.importCsv(entityId, reader,
                        csvFile.getOriginalFilename(), subjectCsvImportCustomizer);
                return results.totalNumberOfImportedInstances();
            }
        } catch (IOException e) {
            throw new CsvImportException("Unable to open uploaded file", e);
        }
    }

    @RequestMapping(value = "/exportDailyClinicVisitScheduleReport", method = RequestMethod.GET)
    public void exportDailyClinicVisitScheduleReport(GridSettings settings,
                                                     @RequestParam String exportRecords,
                                                     @RequestParam String outputFormat,
                                                     HttpServletResponse response) throws IOException {
        exportEntity(settings, exportRecords, outputFormat, response, "DailyClinicVisitScheduleReport",
                null, Visit.class, EbodacConstants.DAILY_CLINIC_VISIT_SCHEDULE_REPORT_MAP);
    }

    @RequestMapping(value = "/exportFollowupsAfterPrimeInjectionReport", method = RequestMethod.GET)
    public void exportFollowupsAfterPrimeInjectionReport(GridSettings settings,
                                                     @RequestParam String exportRecords,
                                                     @RequestParam String outputFormat,
                                                     HttpServletResponse response) throws IOException {
        settings = DtoLookupHelper.changeLookupForFollowupsAfterPrimeInjectionReport(settings);
        if(settings == null) {
            response.sendError(400, "Invalid lookups params");
        } else {
            exportEntity(settings, exportRecords, outputFormat, response, "FollowupsAfterPrimeInjectionReport",
                    null, Visit.class, EbodacConstants.FOLLOW_UPS_AFTER_PRIME_INJECTION_REPORT_MAP);
        }
    }

    @RequestMapping(value = "/exportFollowupsMissedClinicVisitsReport", method = RequestMethod.GET)
    public void exportFollowupsMissedClinicVisitsReport(GridSettings settings,
                                                         @RequestParam String exportRecords,
                                                         @RequestParam String outputFormat,
                                                         HttpServletResponse response) throws IOException {
        settings = DtoLookupHelper.changeLookupAndOrderForFollowupsMissedClinicVisitsReport(settings);
        if(settings == null) {
            response.sendError(400, "Invalid lookups params");
        } else {
            exportEntity(settings, exportRecords, outputFormat, response, "FollowupsMissedClinicVisitsReport",
                    MissedVisitsReportDto.class, Visit.class, EbodacConstants.FOLLOW_UPS_MISSED_CLINIC_VISITS_REPORT_MAP);
        }
    }

    private void exportEntity(GridSettings settings,
                              String exportRecords,
                              String outputFormat,
                              HttpServletResponse response,
                              String fileNameBeginning,
                              Class<?> entityDtoType, Class<?> entityType,
                              Map<String, String> headerMap) throws IOException {

        DateTimeFormatter dateTimeFormatter = DateTimeFormat.forPattern("yyyyMMddHHmmss");
        final String fileName = fileNameBeginning + "_" + DateTime.now().toString(dateTimeFormatter);

        if (EbodacConstants.PDF_EXPORT_FORMAT.equals(outputFormat)) {
            response.setContentType("application/pdf");
        } else if(EbodacConstants.CSV_EXPORT_FORMAT.equals(outputFormat)) {
            response.setContentType("text/csv");
        } else if(EbodacConstants.XLS_EXPORT_FORMAT.equals(outputFormat)) {
            response.setContentType("application/vnd.ms-excel");
        } else {
            throw new IllegalArgumentException("Invalid export format: " + outputFormat);
        }
        response.setCharacterEncoding(UTF_8);
        response.setHeader(
                "Content-Disposition",
                "attachment; filename=" + fileName + "." + outputFormat.toLowerCase());

        Order order = null;
        if (StringUtils.isNotBlank(settings.getSortColumn())) {
            order = new Order(settings.getSortColumn(), settings.getSortDirection());
        }
        QueryParams queryParams = new QueryParams(1, StringUtils.equalsIgnoreCase(exportRecords, "all") ? null : Integer.valueOf(exportRecords), order);

        Config config = configService.getConfig();

        try {
            if (EbodacConstants.PDF_EXPORT_FORMAT.equals(outputFormat)) {
                PdfBasicTemplate template;
                if (entityType.getName().equals(ReportPrimerVaccination.class.getName()) || entityType.getName().equals(ReportBoosterVaccination.class.getName())) {
                    template = new PdfReportATemplate(response.getOutputStream());
                    ((PdfReportATemplate) template).setAdditionalCellValues(fileNameBeginning.replaceAll("([A-Z])", " $1"), "Daily", config.getDistrict());
                } else {
                    template = new PdfReportBTemplate(response.getOutputStream());
                    ((PdfReportBTemplate) template).setAdditionalCellValues(fileNameBeginning.replaceAll("([A-Z])", " $1"), config.getDistrict(), config.getChiefdom());
                }

                exportService.exportEntityToPDF(template, entityDtoType, entityType, headerMap,
                        settings.getLookup(), settings.getFields(), queryParams);
            } else if(EbodacConstants.CSV_EXPORT_FORMAT.equals(outputFormat)) {
                exportService.exportEntityToCSV(response.getWriter(), entityDtoType, entityType, headerMap,
                        settings.getLookup(), settings.getFields(), queryParams);
            } else if(EbodacConstants.XLS_EXPORT_FORMAT.equals(outputFormat)) {
                XlsBasicTemplate template;
                if (entityType.getName().equals(ReportPrimerVaccination.class.getName()) || entityType.getName().equals(ReportBoosterVaccination.class.getName())) {
                    template = new XlsReportATemplate(response.getOutputStream());
                    ((XlsReportATemplate) template).setAdditionalCellValues(fileNameBeginning.replaceAll("([A-Z])", " $1"), "Daily", config.getDistrict());
                } else {
                    template = new XlsReportBTemplate(response.getOutputStream());
                    ((XlsReportBTemplate) template).setAdditionalCellValues(fileNameBeginning.replaceAll("([A-Z])", " $1"), config.getDistrict(), config.getChiefdom());
                }

                exportService.exportEntityToExcel(template, entityDtoType, entityType, headerMap,
                        settings.getLookup(), settings.getFields(), queryParams);
            }
        } catch (IOException | EbodacLookupException | EbodacExportException e) {
            LOGGER.debug(e.getMessage(), e);
            response.sendError(HttpServletResponse.SC_INTERNAL_SERVER_ERROR, e.getMessage());
        }
    }

    private Map<String, Object> getFields(GridSettings gridSettings) throws IOException {
        if (gridSettings.getFields() == null) {
            return null;
        } else {
            return objectMapper.readValue(gridSettings.getFields(), new TypeReference<HashMap>() {});
        }
    }
}
