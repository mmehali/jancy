package eu.tznvy.jancy.modulesgen.discovery.model

/**
  * Represents an Ansible module
  *
  * @param className          the name for the generated class in pascal case
  * @param originalName       the module name in snake case
  * @param namespace          the namespace for the generated class
  * @param description        long module description
  * @param shortDescription   short module description
  * @param options            module parameters' metadata
  * @param documentationFragments   external documentation fragments to merge
  * @param authors            the authors of the original module
  * @param versionAdded       Ansible version in which the module was introduced
  * @param notes              additional notes
  * @param deprecated         deprecation comment. Some("") if marked as deprecated but no comment provided
  */
case class ModuleMetadata(
  className: String,
  originalName: String,
  namespace: String,
  description: Option[String],
  shortDescription: Option[String],
  options: Seq[OptionMetadata],
  documentationFragments: Seq[String],
  authors: Seq[String],
  versionAdded: Option[String],
  notes: Seq[String],
  deprecated: Option[String]
)