package org.motechproject.ebodac.service;

import org.motechproject.ebodac.domain.Language;
import org.motechproject.ebodac.domain.PhoneType;
import org.motechproject.ebodac.domain.SubjectRegistration;

import java.util.List;

/**
 * Service interface for CRUD on SubjectRegistration
 */
public interface SubjectRegistrationService {

    void create(String phoneNumber, String firstName, String lastName, Integer age, String address,
                Language language, PhoneType phoneType);

    void add(SubjectRegistration record);

    SubjectRegistration findRegistrationByFirstName(String firstName);

    List<SubjectRegistration> getAll();

    void delete(SubjectRegistration record);

    void update(SubjectRegistration record);
}
