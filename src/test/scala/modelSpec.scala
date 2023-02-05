import model.{Channel, Package}
import org.scalatest.matchers.must.Matchers.convertToAnyMustWrapper
import org.scalatest.wordspec.AsyncWordSpec

class modelSpec extends AsyncWordSpec{
"verify if package is valid or not" in {
  val newPackage1 = Package("basic",List(Channel("DDNational")))
  val newPackage2 = Package("prime",List(Channel("DDNational")))
  Package.validPackage(newPackage1.name) mustBe true
  Package.validPackage(newPackage2.name) mustBe false
}
}
