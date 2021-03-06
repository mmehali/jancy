package eu.tznvy.jancy.modulesgen.specialcases.modules

import eu.tznvy.jancy.modulesgen.helpers.CapitalizationHelper
import eu.tznvy.jancy.modulesgen.discovery.model.Choice
import eu.tznvy.jancy.modulesgen.specialcases.SpecialCase

object IpmiPower extends SpecialCase {
  override def moduleName: String = "ipmi_power"

  override def optionsWithCustomEnumBuilder = Map(
    "state" -> makeAnEnum)

  private def makeAnEnum(choices: Seq[String]): Seq[Choice] =
    choices.map({ c =>
      val sides = c.split("--", 2)
      val name = sides(0).trim
      val comment = sides(1).trim

      Choice(
        CapitalizationHelper.snakeCaseToAllCaps(name),
        name)
    })
}
