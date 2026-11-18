package mx.edu.uacm.is.slt.as.IDSISTEMA.architecture;

import com.tngtech.archunit.core.importer.ImportOption;
import com.tngtech.archunit.junit.AnalyzeClasses;
import com.tngtech.archunit.junit.ArchTest;
import com.tngtech.archunit.lang.ArchRule;
import static com.tngtech.archunit.lang.syntax.ArchRuleDefinition.noClasses;

/**
 * Reglas de dependencia de la Arquitectura Limpia.
 * domain no importa Spring ni capas externas; application solo conoce domain;
 * ui.web solo invoca puertos de entrada (port.in) y nunca adaptadores ni
 * servicios concretos.
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
    static final ArchRule UI_WEB_SOLO_INVOCA_PUERTOS_DE_ENTRADA = noClasses()
            .that().resideInAPackage("..ui.web..")
            .should().dependOnClassesThat().resideInAnyPackage(
                    "..application.service..",
                    "..application.port.out..",
                    "..adapter..",
                    "..infrastructure..")
            .allowEmptyShould(true);

    @ArchTest
    static final ArchRule ADAPTADORES_WEB_NO_INVOCAN_SERVICIOS_CONCRETOS = noClasses()
            .that().resideInAPackage("..adapter.in.web..")
            .should().dependOnClassesThat().resideInAnyPackage(
                    "..application.service..",
                    "..application.port.out..",
                    "..adapter.out..")
            .allowEmptyShould(true);

    @ArchTest
    static final ArchRule CLIENTES_DOMAIN_AISLADO = noClasses()
            .that().resideInAnyPackage("..clientes.domain..", "..clientes.application..")
            .should().dependOnClassesThat().resideInAnyPackage(
                    "..polizas..", "..beneficiarios..")
            .allowEmptyShould(true);

    @ArchTest
    static final ArchRule POLIZAS_CORE_AISLADO = noClasses()
            .that().resideInAnyPackage("..polizas.domain..", "..polizas.application..")
            .should().dependOnClassesThat().resideInAnyPackage(
                    "..clientes..", "..beneficiarios..")
            .allowEmptyShould(true);

    @ArchTest
    static final ArchRule BENEFICIARIOS_CORE_AISLADO = noClasses()
            .that().resideInAnyPackage("..beneficiarios.domain..", "..beneficiarios.application..")
            .should().dependOnClassesThat().resideInAnyPackage(
                    "..clientes..", "..polizas..")
            .allowEmptyShould(true);
}
