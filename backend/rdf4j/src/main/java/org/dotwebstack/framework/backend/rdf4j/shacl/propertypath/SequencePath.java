package org.dotwebstack.framework.backend.rdf4j.shacl.propertypath;

import java.util.ArrayList;
import java.util.List;

import lombok.Builder;
import lombok.Getter;
import lombok.Setter;

import org.eclipse.rdf4j.model.IRI;
import org.eclipse.rdf4j.sail.memory.model.MemBNode;
import org.eclipse.rdf4j.sail.memory.model.MemStatement;
import org.eclipse.rdf4j.sail.memory.model.MemStatementList;
import org.eclipse.rdf4j.sail.memory.model.MemValue;

@Builder
@Getter
@Setter
public class SequencePath extends PropertyPath {

  PropertyPath first;

  PropertyPath rest;

}
