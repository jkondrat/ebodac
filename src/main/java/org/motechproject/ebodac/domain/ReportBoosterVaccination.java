package org.motechproject.ebodac.domain;

import org.joda.time.DateTime;
import org.motechproject.mds.annotations.Entity;
import org.motechproject.mds.annotations.Field;
import org.motechproject.mds.annotations.UIDisplayable;

@Entity
public class ReportBoosterVaccination extends ReportVaccinationAbstract {

    @UIDisplayable(position = 1)
    @Field
    private Integer peopleBoostered;

    public ReportBoosterVaccination() {
    }

    public ReportBoosterVaccination(DateTime date, Integer adultMales, Integer adultFemales, Integer children_0_5,
                                    Integer children_6_11, Integer children_12_17, Integer peopleBoostered) {
        super(date, adultMales, adultFemales, children_0_5, children_6_11, children_12_17);
        this.peopleBoostered = peopleBoostered;
    }

    public Integer getPeopleBoostered() {
        return peopleBoostered;
    }

    public void setPeopleBoostered(Integer peopleBoostered) {
        this.peopleBoostered = peopleBoostered;
    }

    public void updateReportData(Integer adultMales, Integer adultFemales, Integer children_0_5,
                                 Integer children_6_11, Integer children_12_17, Integer peopleBoostered) {
        updateReportData(adultMales, adultFemales, children_0_5, children_6_11, children_12_17);
        this.peopleBoostered = peopleBoostered;
    }
}
