package org.motechproject.ebodac.repository;

import org.joda.time.DateTime;
import org.motechproject.commons.api.Range;
import org.motechproject.ebodac.domain.Subject;
import org.motechproject.mds.annotations.Lookup;
import org.motechproject.mds.annotations.LookupField;
import org.motechproject.mds.service.MotechDataService;

import java.util.List;

/**
 * Interface for repository that persists simple records and allows CRUD.
 * MotechDataService base class will provide the implementation of this class as well
 * as methods for adding, deleting, saving and finding all instances.  In this class we
 * define and custom lookups we may need.
 */
public interface SubjectDataService extends MotechDataService<Subject> {

    @Lookup
    Subject findSubjectByName(@LookupField(name = "name") String name);

    @Lookup
    Subject findSubjectBySubjectId(@LookupField(name = "subjectId") String subjectId);

    @Lookup
    List<Subject> findSubjectsByModified(@LookupField(name = "changed") Boolean modified);

    @Lookup
    List<Subject> findSubjectsByPrimerVaccinationDate(@LookupField(name = "primerVaccinationDate") Range<DateTime> dateRange);

    @Lookup
    List<Subject> findSubjectsByBoosterVaccinationDate(@LookupField(name = "boosterVaccinationDate") Range<DateTime> dateRange);
}
