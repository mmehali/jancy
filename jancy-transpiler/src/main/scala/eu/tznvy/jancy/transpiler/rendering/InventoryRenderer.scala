package eu.tznvy.jancy.transpiler.rendering

import eu.tznvy.jancy.core.{Host, Inventory, Group}

object InventoryRenderer extends Renderer[Inventory] {
  def render(inventory: Inventory): String = {
    def renderHosts(hosts: Iterable[Host]) = {
      val lines = hosts.map(_.getName).mkString("\n")
      if (lines.length > 0) lines + "\n\n"
      else ""
    }

    def renderGroups(groups: Iterable[Group]) =
      groups.map({ g => s"[${g.getName}]\n${renderHosts(g.getHosts)}" }).mkString

    val standaloneHosts =
      inventory.getHosts.toSet -- inventory.getGroups.flatMap(_.getHosts)

    (renderHosts(standaloneHosts) + renderGroups(inventory.getGroups)).trim
  }

  override def renderAll(ts: Array[Inventory]) = ???
}
