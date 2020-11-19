package org.openmrs.module.kenyaemrorderentry.fragment.controller.patientdashboard;

import com.fasterxml.jackson.databind.node.ObjectNode;
import org.apache.commons.beanutils.PropertyUtils;
import org.openmrs.CareSetting;
import org.openmrs.EncounterRole;
import org.openmrs.EncounterType;
import org.openmrs.OrderType;
import org.openmrs.Patient;
import org.openmrs.api.ConceptService;
import org.openmrs.api.EncounterService;
import org.openmrs.api.ObsService;
import org.openmrs.api.OrderService;
import org.openmrs.api.OrderSetService;
import org.openmrs.api.PatientService;
import org.openmrs.api.ProviderService;
import org.openmrs.api.context.Context;
import org.openmrs.module.appui.UiSessionContext;
import org.openmrs.module.kenyaemrorderentry.labDataExchange.LabOrderDataExchange;
import org.openmrs.module.webservices.rest.web.ConversionUtil;
import org.openmrs.module.webservices.rest.web.representation.Representation;
import org.openmrs.ui.framework.SimpleObject;
import org.openmrs.ui.framework.UiUtils;
import org.openmrs.ui.framework.annotation.SpringBean;
import org.openmrs.ui.framework.fragment.FragmentConfiguration;
import org.openmrs.ui.framework.fragment.FragmentModel;
import org.springframework.web.bind.annotation.RequestParam;

import java.util.LinkedHashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;

public class GeneralLabOrdersFragmentController {
    public static final Locale LOCALE = Locale.ENGLISH;
    ConceptService concService = Context.getConceptService();

    public void controller(FragmentConfiguration config,
                          // @RequestParam("patient") Patient patient,
                           @RequestParam(value = "careSetting", required = false) CareSetting careSetting,
                           @SpringBean("encounterService") EncounterService encounterService,
                           @SpringBean("orderService") OrderService orderService,
                           UiSessionContext sessionContext,
                           UiUtils ui,
                           FragmentModel model,
                           @SpringBean("orderSetService") OrderSetService orderSetService,
                           @SpringBean("patientService") PatientService patientService,
                           @SpringBean("conceptService") ConceptService conceptService,
                           @SpringBean("providerService") ProviderService providerService,
                           @SpringBean("obsService") ObsService obsService) throws Exception {
        config.require("patient|patientId");
        Patient patient;
        Object pt = config.getAttribute("patient");
        if (pt == null) {
            patient = patientService.getPatient((Integer) config.getAttribute("patientId"));
        }
        else {
            // in case we are passed a PatientDomainWrapper (but this module doesn't know about emrapi)
            patient = (Patient) (pt instanceof Patient ? pt : PropertyUtils.getProperty(pt, "patient"));
        }
        EncounterType labOrderEncounterType = encounterService.getEncounterTypeByUuid(OrderType.TEST_ORDER_TYPE_UUID);
        EncounterRole encounterRoles = encounterService.getAllEncounterRoles(false).get(0);

        List<CareSetting> careSettings = orderService.getCareSettings(false);

        Map<String, Object> jsonConfig = new LinkedHashMap<String, Object>();
        jsonConfig.put("patient", convertToFull(patient));
        jsonConfig.put("provider", convertToFull(sessionContext.getCurrentProvider()));
        jsonConfig.put("encounterRole", convertToFull(encounterRoles));
        jsonConfig.put("labOrderEncounterType", convertToFull(labOrderEncounterType));
        jsonConfig.put("careSettings", convertToFull(careSettings));

        if (careSetting != null) {
            jsonConfig.put("intialCareSetting", careSetting.getUuid());
        }

        model.put("patient", patient);
        model.put("jsonConfig", ui.toJson(jsonConfig));

    }
    private Object convertTo(Object object, Representation rep) {
        return object == null ? null : ConversionUtil.convertToRepresentation(object, rep);
    }

    private Object convertToFull(Object object) {
        return object == null ? null : ConversionUtil.convertToRepresentation(object, Representation.FULL);
    }

    public SimpleObject generateViralLoadPayload() {

        LabOrderDataExchange dataExchange = new LabOrderDataExchange();
        ObjectNode payload = dataExchange.getLabRequests(null, null);

        System.out.println("Generated payload:::::" + payload.toString());

        SimpleObject simpleObject = SimpleObject.create("status", "successful");
        return simpleObject;
    }
}
