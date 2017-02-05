package ru.nullpointer.nkbrelation.service.openapi;

import org.apache.commons.collections.CollectionUtils;
import org.apache.commons.collections.MapUtils;
import org.apache.commons.lang3.time.DateFormatUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import ru.nullpointer.nkbrelation.api.rest.open.domain.*;
import ru.nullpointer.nkbrelation.domain.NodeInfo;
import ru.nullpointer.nkbrelation.service.HistoryService;

import javax.annotation.Resource;
import java.util.*;

/**
 * @author ankostyuk
 */
@Service
public class OpenAPIHelper {

    private static Logger logger = LoggerFactory.getLogger(OpenAPIHelper.class);
    //
    private static final String DATE_PATTERN = "yyyy-MM-dd";
    //
    @Resource
    private HistoryService historyService;
    //

    public boolean directionToChildren(String direction) {
        return "out".equals(direction);
    }

    public PublicNode buildPublicNode(Map<String, Object> node) {
        String nodeType = (String) node.get("_type");

        if ("COMPANY".equals(nodeType)) {
            return buildCompanyFromNode(node);
        } else if ("INDIVIDUAL".equals(nodeType)) {
            return buildIndividualFromNode(node);
        } else if ("ADDRESS".equals(nodeType)) {
            return buildAddressFromNode(node);
        } else if ("PHONE".equals(nodeType)) {
            return buildPhoneFromNode(node);
        } else if ("PURCHASE".equals(nodeType)) {
            return buildPurchaseFromNode(node);
        }

        return null;
    }

    public Company buildCompanyFromNode(Map<String, Object> node) {
        Company company = new Company();
        nodeToCompany(node, company);
        return company;
    }

    public Individual buildIndividualFromNode(Map<String, Object> node) {
        Individual individual = new Individual();
        nodeToIndividual(node, individual);
        return individual;
    }

    public Address buildAddressFromNode(Map<String, Object> node) {
        Address address = new Address();
        nodeToAddress(node, address);
        return address;
    }

    public Phone buildPhoneFromNode(Map<String, Object> node) {
        Phone phone = new Phone();
        nodeToPhone(node, phone);
        return phone;
    }

    public Purchase buildPurchaseFromNode(Map<String, Object> node) {
        Purchase purchase = new Purchase();
        nodeToPurchase(node, purchase);
        return purchase;
    }

    private void nodeToPublicNode(Map<String, Object> node, PublicNode publicNode) {
        publicNode.setId(MapUtils.getString(node, "_id"));
        setPublicNodeInfo(node, publicNode);
    }

    private void setPublicNodeInfo(Map<String, Object> node, PublicNode publicNode) {
        NodeInfo _info = (NodeInfo) MapUtils.getObject(node, "_info");

        if (_info == null) {
            return;
        }

        NodeInfo info = new NodeInfo();

        info.setIn(_info.getIn());
        info.setOut(_info.getOut());

        publicNode.setInfo(info);
    }

    public void nodeToCompany(Map<String, Object> node, Company company) {
        nodeToPublicNode(node, company);

        company.setInn(MapUtils.getString(node, "inn"));
        company.setOgrn(MapUtils.getString(node, "ogrn"));
        company.setOkpo(MapUtils.getString(node, "okpo"));

        company.setKpp(MapUtils.getString(node, "kpp"));
        company.setOkved(MapUtils.getString(node, "okvedcode_main"));
        company.setOkato(MapUtils.getString(node, "okatocode"));
        company.setOkopf(MapUtils.getString(node, "okopfcode"));

        company.setAddress(MapUtils.getString(node, "addresssort"));
        company.setChiefName(MapUtils.getString(node, "chief_name"));
        company.setFullName(MapUtils.getString(node, "namesort"));
        company.setShortName(MapUtils.getString(node, "nameshortsort"));

        company.setRegDate(getDateAsString(node, "founded_dt"));

        setCompanyStatus(company, node);
    }

    private void setCompanyStatus(Company company, Map<String, Object> node) {
        @SuppressWarnings("unchecked")
        Map<String, Object> egrulState = (Map<String, Object>) node.get("egrul_state");

        if (egrulState == null) {
            return;
        }

        Company.Status status = new Company.Status();
        status.setType((String) egrulState.get("type"));
        status.setDate(getDateAsString(egrulState, "_actual"));

        company.setStatus(status);
    }

    public void nodeToIndividual(Map<String, Object> node, Individual individual) {
        nodeToPublicNode(node, individual);

        individual.setName(MapUtils.getString(node, "name"));
    }

    public void nodeToAddress(Map<String, Object> node, Address address) {
        nodeToPublicNode(node, address);

        address.setIndex(MapUtils.getString(node, "index"));
        address.setValue(MapUtils.getString(node, "value"));
    }

    public void nodeToPhone(Map<String, Object> node, Phone phone) {
        nodeToPublicNode(node, phone);

        phone.setValue(MapUtils.getString(node, "value"));
    }

    public void nodeToPurchase(Map<String, Object> node, Purchase purchase) {
        nodeToPublicNode(node, purchase);

        purchase.setName(MapUtils.getString(node, "name"));
        purchase.setForm(MapUtils.getString(node, "form"));
        purchase.setCurrency(MapUtils.getString(node, "currency"));
        purchase.setTotalPrice(MapUtils.getNumber(node, "total_price"));
        purchase.setEtp(MapUtils.getString(node, "etp"));
        purchase.setLaw(MapUtils.getString(node, "law"));
        purchase.setDate(getDateAsString(node, "date"));
    }

    @SuppressWarnings("unchecked")
    public void transformToPublicRelations(List<Map<String, Object>> relations) {
        if (CollectionUtils.isEmpty(relations)) {
            return;
        }

        Comparator byOutdated = Comparator.comparing((Map<String, Object> r) -> (Boolean) r.get("outdated"), Comparator.nullsLast(Comparator.<Boolean>naturalOrder()));
        Comparator bySince = Comparator.comparing((Map<String, Object> r) -> (Long) r.get("_since"), Comparator.nullsLast(Comparator.<Long>reverseOrder()));
        Comparator byActual = Comparator.comparing((Map<String, Object> r) -> (Long) r.get("_actual"), Comparator.nullsLast(Comparator.<Long>reverseOrder()));
        relations.sort(byOutdated.thenComparing(bySince).thenComparing(byActual));

        String relationTypeName = (String) relations.get(0).get("_type");

        if (!historyService.isHistoryRelationType(relationTypeName)) {
            Map<String, Object> firstRelation = relations.get(0);
            relations.clear();
            relations.add(firstRelation);
        }

        for (Map<String, Object> relation : relations) {
            transformToPublicRelation(relation);
        }
    }

    private void transformToPublicRelation(Map<String, Object> relation) {
        relation.put("actual", getDateAsString(relation, "_actual"));
        relation.put("since", getDateAsString(relation, "_since"));
        relation.entrySet().removeIf(e -> e.getKey().startsWith("_"));
    }

    private String getDateAsString(Map<String, Object> map, String key) {
        Object date = map.get(key);

        if (date == null) {
            return null;
        }

        if (Long.class.equals(date.getClass())) {
            return DateFormatUtils.format((Long) date, DATE_PATTERN);
        } else
        if (Date.class.equals(date.getClass())) {
            return DateFormatUtils.format((Date) date, DATE_PATTERN);
        }

        return null;
    }
}
