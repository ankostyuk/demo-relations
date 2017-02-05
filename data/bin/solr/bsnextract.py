#!/usr/bin/env python
# -*- coding: utf-8 -*-
import os
import hashlib
import string
import json
import sys

import xml.etree.ElementTree as xml
from xml.etree.ElementTree import ElementTree, Element, SubElement


def main():
    f = sys.argv[1]
    path = os.path.dirname(f)

    address = init_solr_xml()
    individual = init_solr_xml()
    phone = init_solr_xml()

    relations = open(os.path.join(path, 'relations.json'), 'wb')
    props = open(os.path.join(path, 'properties.json'), 'wb')

    tree = xml.parse(f)
    docs = tree.findall('doc')

    counts = {
        'total': 0,
        'individual': 0,
        'address': 0,
        'phone': 0
    }
    for doc in docs:
        counts['total'] += 1
        id = doc.find('field[@name="bsn_id"]').text
        company_id = 'COMPANY.' + id

        x = extract_address(doc)
        append_doc(address, x)
        append_relation(relations, 'ADDRESS.' + x['id'], company_id, 'ADDRESS')
        counts['address'] += 1

        x = extract_chief(doc)
        append_doc(individual, x)
        append_relation(relations, 'INDIVIDUAL.' + x['id'], company_id, 'EXECUTIVE_INDIVIDUAL')
        counts['individual'] += 1

        x = extract_phone_field(doc, 'contact_fax_legal', 'fax_legal')
        if x:
            append_doc(phone, x)
            append_relation(relations, 'PHONE.' + x['id'], company_id, 'PHONE')
            counts['phone'] += 1

        x = extract_phone_field(doc, 'contact_fax_actual', 'fax_actual')
        if x:
            append_doc(phone, x)
            append_relation(relations, 'PHONE.' + x['id'], company_id, 'PHONE')
            counts['phone'] += 1

        x = extract_phone_field(doc, 'contact_phone_legal', 'phone_legal')
        if x:
            append_doc(phone, x)
            append_relation(relations, 'PHONE.' + x['id'], company_id, 'PHONE')
            counts['phone'] += 1

        x = extract_phone_field(doc, 'contact_phone_actual', 'phone_actual')
        if x:
            append_doc(phone, x)
            append_relation(relations, 'PHONE.' + x['id'], company_id, 'PHONE')
            counts['phone'] += 1

        codes = {}
        x = extract(doc, 'ogrn')
        if x is not None:
            codes['ogrn'] = long(x)

        x = extract(doc, 'inn')
        if x is not None:
            codes['inn'] = long(x)

        append_codes(props, id, codes)

    write_solr_xml(address, os.path.join(path, 'address.xml'))
    write_solr_xml(individual, os.path.join(path, 'individual.xml'))
    write_solr_xml(phone, os.path.join(path, 'phone.xml'))

    relations.close()
    props.close()

    print 'Total: {}'.format(counts['total'])
    print 'individual: {}'.format(counts['individual'])
    print 'address: {}'.format(counts['address'])
    print 'phone: {}'.format(counts['phone'])


def init_solr_xml():
    e = Element('add')
    e.set('overwrite', 'true')
    return e


def write_solr_xml(e, path):
    indent(e)
    ElementTree(e).write(path, encoding='utf-8', xml_declaration=True)


def extract_address(doc):
    value = doc.find('field[@name="addresssort"]').text
    index = doc.find('field[@name="addressindex"]').text
    id = hashlib.sha1((index + value).encode('utf-8')).hexdigest()

    return {
        'id': id,
        'index': index,
        'value': value,
    }


def extract_chief(doc):
    name = doc.find('field[@name="chief_name"]').text
    id = hashlib.sha1(name.lower().encode('utf-8')).hexdigest()

    return {
        'id': id,
        'name': name,
    }


def extract_phone_field(doc, name, t):
    value = doc.find('field[@name="' + name + '"]')
    if value is None:
        return None

    value = value.text

    clean_value = ''.join(c for c in value if c.isdigit())

    return {
        'id': t + clean_value,
        'phoneType': t,
        'value': value,
    }


def extract(doc, field):
    e = doc.find('field[@name="' + field + '"]')
    if e is not None:
        return e.text
    return None


def append_doc(root, d):
    e = SubElement(root, 'doc')

    for k in d.keys():
        f = SubElement(e, 'field')
        f.set('name', k)
        f.text = d[k]


def append_relation(relations, src, dst, t):
    rel = {
        '_type': t,
        '_srcId': src,
        '_dstId': dst,
        '_holderDst': True,
    }
    relations.write(json.dumps(rel, ensure_ascii=False))
    relations.write('\n')


def append_codes(props, id, codes):
    c = {
        'id': id,
        'type': 'COMPANY'
    }
    c = dict(c.items() + codes.items())
    props.write(json.dumps(c, ensure_ascii=False))
    props.write('\n')


def indent(elem, level=0):
    i = "\n" + level*"  "
    if len(elem):
        if not elem.text or not elem.text.strip():
            elem.text = i + "  "
        if not elem.tail or not elem.tail.strip():
            elem.tail = i
        for elem in elem:
            indent(elem, level+1)
        if not elem.tail or not elem.tail.strip():
            elem.tail = i
    else:
        if level and (not elem.tail or not elem.tail.strip()):
            elem.tail = i


if __name__ == '__main__':
    main()

