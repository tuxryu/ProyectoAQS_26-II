package mx.edu.uacm.is.slt.as.IDSISTEMA.architecture;

import com.tngtech.archunit.core.importer.ImportOption;
import com.tngtech.archunit.junit.AnalyzeClasses;
import com.tngtech.archunit.junit.ArchTest;
import com.tngtech.archunit.lang.ArchRule;
import static com.tngtech.archunit.lang.syntax.ArchRuleDefinition.noClasses;

/**
 * Reglas de dependencia de la Arquitectura Limpia.
 * domain no importa Spring ni capas externas; application solo conoce domain.
 */
@AnalyzeClasses(
        packages = "mx.edu.uacm.is.slt.as.IDSISTEMA",
        importOptions = ImportOption.DoNotIncludeTests.class)
class ArquitecturaLimpiaTest {

    @ArchTest
    static final ArchRule DOMAIN_NO_DEPENDE_DE_SPRING = noClasses()
            .that().resideInAPackage("..domain..")
            .should().dependOnClassesThat().resideInAnyPackage(
                    "org.springframework..", "..application..", "..adapter..", "..ui..")
            .allowEmptyShould(true);

    @ArchTest
    static final ArchRule APPLICATION_NO_DEPENDE_DE_ADAPTADORES = noClasses()
            .that().resideInAPackage("..application..")
            .should().dependOnClassesThat().resideInAnyPackage(
                    "..adapter..", "..ui..", "org.springframework.web..")
            .allowEmptyShould(true);

    @ArchTest
    static final ArchRule CLIENTES_DOMAIN_AISLADO = noClasses()
            .that().resideInAPackage("..clientes.domain..")
            .should().dependOnClassesThat().resideInAnyPackage(
                    "..polizas..", "..beneficiarios..")
            .allowEmptyShould(true);

}
