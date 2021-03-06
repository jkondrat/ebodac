package org.motechproject.ebodac.constants;

import java.util.regex.Pattern;

public final class EbodacConstants {

    public static final String UI_CONFIG = "custom-ui.js";

    public static final String SUBJECT_ID_FIELD_NAME = "subjectId";

    public static final String SITE_ID_FOR_STAGE_I = "B05-SL10001";

    public static final String ZETES_UPDATE_EVENT = "zetes_update";
    public static final String EMAIL_CHECK_EVENT = "email_check";

    public static final String ZETES_URL = "zetes_url";
    public static final String ZETES_USERNAME = "zetes_username";
    public static final String ZETES_PASSWORD = "zetes_password";
    public static final String START_TIME = "start_time";

    public static final Pattern CSV_FILENAME_PATTERN = Pattern.compile(".*_(.*_.*)\\.csv");
    public static final String CSV_DATE_FORMAT = "yyyyMMdd_HHmmss";

    public static final String FETCHED_EMAIL_FLAG = "fetched";
    public static final String JOB_SUCCESS_STATUS = "COMPLETION";
    public static final String JOB_FAILURE_STATUS = "FAILURE";

    public static final String DAILY_REPORT_EVENT = "daily_report_event";
    public static final String DAILY_REPORT_EVENT_START_DATE = "daily_report_event_start_date";
    public static final String DAILY_REPORT_EVENT_START_HOUR = "00:01";
    public static final String LAST_REPORT_DEFAULT_DATE = "2015-06-01";
}
