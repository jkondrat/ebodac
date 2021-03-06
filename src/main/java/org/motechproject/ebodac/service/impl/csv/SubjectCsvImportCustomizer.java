package org.motechproject.ebodac.service.impl.csv;

import org.apache.commons.lang.StringUtils;
import org.motechproject.ebodac.constants.EbodacConstants;
import org.motechproject.ebodac.domain.Subject;
import org.motechproject.ebodac.service.SubjectService;
import org.motechproject.mds.service.CsvImportCustomizer;
import org.motechproject.mds.service.MotechDataService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.util.Map;

@Component
public class SubjectCsvImportCustomizer implements CsvImportCustomizer {

    @Autowired
    SubjectService subjectService;

    @Override
    public Object findExistingInstance(Map<String, String> row, MotechDataService motechDataService) {
        String subjectId = row.get(EbodacConstants.SUBJECT_ID_FIELD_NAME);

        if (StringUtils.isNotBlank(subjectId)) {
            return subjectService.findSubjectBySubjectId(subjectId);
        }
        return null;
    }

    @Override
    public Object doCreate(Object instance, MotechDataService motechDataService) {
        return subjectService.create((Subject) instance, true);
    }

    @Override
    public Object doUpdate(Object instance, MotechDataService motechDataService) {
        return subjectService.update((Subject) instance, true);
    }
}
