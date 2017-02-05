package ru.nullpointer.nkbrelation.service.transform.derived;

import creditnet.classifier.entity.Okved;
import creditnet.db.rest.client.resource.classifier.OkvedInfoResource;

/**
 *
 * @author ankostyuk
 */
public class ShortOkvedClassifier implements Classifier {

    private OkvedInfoResource okvedInfoResource;

    @Override
    public String getText(String code, String lang) {
        if (code == null || code.length() < 2) {
            return null;
        }

        String shortCode = code.substring(0, 2);

        Okved okved = okvedInfoResource.findByCodeAndLang(shortCode, lang);

        if (okved == null) {
            return null;
        }

        return okved.getName();
    }

    public void setOkvedInfoResource(OkvedInfoResource okvedInfoResource) {
        this.okvedInfoResource = okvedInfoResource;
    }
}
