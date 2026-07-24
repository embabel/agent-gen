package com.embabel.metaagent.kycdemo.screening

import org.w3c.dom.Document
import org.w3c.dom.Element
import org.w3c.dom.NodeList
import java.nio.file.Path
import javax.xml.XMLConstants
import javax.xml.parsers.DocumentBuilderFactory

fun Path.parseXmlDocument(): Document =
    DocumentBuilderFactory.newInstance()
        .apply {
            setFeature(XMLConstants.FEATURE_SECURE_PROCESSING, true)
            isExpandEntityReferences = false
        }
        .newDocumentBuilder()
        .parse(toFile())

fun NodeList.asElements(): List<Element> =
    (0 until length).mapNotNull { item(it) as? Element }

fun Element.childElements(tagName: String): List<Element> =
    getElementsByTagName(tagName).asElements()

fun Element.childTextValues(tagName: String): List<String> =
    childElements(tagName)
        .mapNotNull { it.textContent?.trim() }
        .filter { it.isNotBlank() }

fun Element.firstChildTextOrNull(tagName: String): String? =
    childTextValues(tagName).firstOrNull()

fun Element.attributeOrNull(name: String): String? =
    getAttribute(name).takeIf { it.isNotBlank() }
