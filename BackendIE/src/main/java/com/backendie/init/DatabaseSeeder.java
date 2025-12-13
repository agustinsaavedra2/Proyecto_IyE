package com.backendie.init;

import com.backendie.models.*;
import com.backendie.repository.*;
import lombok.RequiredArgsConstructor;
import org.springframework.boot.CommandLineRunner;
import org.springframework.stereotype.Component;
import org.springframework.security.crypto.password.PasswordEncoder;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

@Component
@RequiredArgsConstructor
public class DatabaseSeeder implements CommandLineRunner {

    private final CategoriaIndustriaRepository categoriaRepository;
    private final PlanRepository planRepository;
    private final EmpresaRepository empresaRepository;
    private final UsuarioRepository usuarioRepository;
    private final SuscripcionRepository suscripcionRepository;

    private final RegulacionRepository regulacionRepository;
    private final PoliticaEmpresaRepository politicaRepo;
    private final ProtocoloRepository protocoloRepo;
    private final ProcedimientoRepository procedimientoRepo;
    private final AuditoriaRepository auditoriaRepo;
    private final OllamaResponseRepository ollamaRepo;
    private final RiesgoRepository riesgoRepo;

    private final PasswordEncoder passwordEncoder;

    @Override
    public void run(String... args) throws Exception {
        seedCategorias();
        seedPlanes();
        seedEmpresaYUsuarios();
        seedMongoDocuments();
        System.out.println("Database seeding completado.");
    }

    private void seedCategorias() {
        if (categoriaRepository.count() > 0) return;
        CategoriaIndustria finansas = CategoriaIndustria.builder()
                .nombre("Finanzas")
                .descripcion("Empresas del sector financiero y bancario.")
                .regulaciones(List.of("Regulación financiera 2023" , "Ley de protección de datos"))
                .build();

        CategoriaIndustria salud = CategoriaIndustria.builder()
                .nombre("Salud")
                .descripcion("Clinicas y prestamos de servicios de salud.")
                .regulaciones(List.of("Norma sanitaria 2022", "Regulación de datos de pacientes"))
                .build();
        categoriaRepository.save(finansas);
        categoriaRepository.save(salud);
    }

    private void seedPlanes() {
        if (planRepository.count() > 0) return;
        Plan basic = Plan.builder()
                .nombre("Básico")
                .precio(0.0)
                .duracionMeses(1)
                .maxUsuarios(5)
                .maxConsultasMensuales(10)
                .unlimited(false)
                .build();

        Plan estandar = Plan.builder()
                .nombre("Estándar")
                .precio(15.00)
                .duracionMeses(6)
                .maxUsuarios(25)
                .maxConsultasMensuales(100)
                .unlimited(false)
                .build();

        Plan premium = Plan.builder()
                .nombre("Premium")
                .precio(20.00)
                .duracionMeses(12)
                .maxUsuarios(100)
                .maxConsultasMensuales(null)
                .unlimited(true)
                .build();

        planRepository.save(basic);
        planRepository.save(estandar);
        planRepository.save(premium);
    }

    private void seedEmpresaYUsuarios() {
        if (empresaRepository.count() > 0) return;
        List<CategoriaIndustria> categorias = categoriaRepository.findAll();
        Long catFin = categorias.stream().filter(c -> c.getNombre().equalsIgnoreCase("Finanzas")).findFirst().map(CategoriaIndustria::getId).orElse(null);
        Long catSalud = categorias.stream().filter(c -> c.getNombre().equalsIgnoreCase("Salud")).findFirst().map(CategoriaIndustria::getId).orElse(null);

        //Empresa A - Finanzas
        Empresa empresaA = Empresa.builder()
                .categoriaId(catFin)
                .nombre("Banco Continental")
                .codigoEmpresa("BC-001")
                .empleados(new ArrayList<>())
                .ubicacion("Bogotá, Colombia")
                .descripcion("Banco comercial de alcance nacional.")
                .build();
        empresaA.setStatus("active");
        empresaRepository.save(empresaA);

        //Admin A
        Usuario adminA = Usuario.builder()
                .empresaId(empresaA.getId())
                .nombre("María López")
                .email("maria.lopez@bancocontinental.com")
                .passwordHash(passwordEncoder.encode("password123"))
                .rol("admin")
                .tokenVersion(0)
                .activo(true)
                .build();
        adminA = usuarioRepository.save(adminA);
        empresaA.getEmpleados().add(adminA.getId());
        empresaRepository.save(empresaA);

        // User A1
        Usuario userA1 = Usuario.builder()
                .empresaId(empresaA.getId())
                .nombre("Carlos Pérez")
                .email("carlos.perez@bancocontinental.com")
                .passwordHash(passwordEncoder.encode("password123"))
                .rol("complianceofficer")
                .tokenVersion(0)
                .activo(true)
                .build();
        userA1 = usuarioRepository.save(userA1);
        empresaA.getEmpleados().add(userA1.getId());
        empresaRepository.save(empresaA);

        // Empresa B - Salud
        Empresa empresaB = Empresa.builder()
                .categoriaId(catSalud)
                .nombre("Clínica Nueva Vida")
                .codigoEmpresa("CNV-100")
                .empleados(new ArrayList<>())
                .ubicacion("Medellín, Colombia")
                .descripcion("Clínica privada con 120 camas")
                .build();
        empresaB.setStatus("active");
        empresaB = empresaRepository.save(empresaB);

        // Admin B
        Usuario adminB = Usuario.builder()
                .empresaId(empresaB.getId())
                .nombre("Andrés Gómez")
                .email("andres.gomez@clinicavida.com")
                .passwordHash(passwordEncoder.encode("password123"))
                .rol("admin")
                .tokenVersion(0)
                .activo(true)
                .build();
        adminB = usuarioRepository.save(adminB);
        empresaB.getEmpleados().add(adminB.getId());
        empresaRepository.save(empresaB);

        // Usuario B1
        Usuario userB1 = Usuario.builder()
                .empresaId(empresaB.getId())
                .nombre("Lucía Ramírez")
                .email("lucia.ramirez@clinicavida.com")
                .passwordHash(passwordEncoder.encode("password123"))
                .rol("auditor")
                .tokenVersion(0)
                .activo(true)
                .build();
        userB1 = usuarioRepository.save(userB1);
        empresaB.getEmpleados().add(userB1.getId());
        empresaRepository.save(empresaB);

        // Suscripciones
        Plan estandarPlan = planRepository.findByNombre("Estándar");
        if (estandarPlan != null) {
            Suscripcion s1 = Suscripcion.builder()
                    .empresaId(empresaA.getId())
                    .plan(estandarPlan.getNombre())
                    .maxUsuarios(estandarPlan.getMaxUsuarios())
                    .fechaFin(LocalDateTime.now().plusMonths(estandarPlan.getDuracionMeses()))
                    .precio(estandarPlan.getPrecio())
                    .build();
            suscripcionRepository.save(s1);
        }
    }

    private void seedMongoDocuments() {
        if (regulacionRepository.count() == 0) {
            Regulacion reg1 = Regulacion.builder()
                    .nombre("Ley de protección de datos")
                    .contenido("Regula el tratamiento de datos personales")
                    .urlDocumento("http://gov/regla1.pdf")
                    .entidadEmisora("Superintendencia")
                    .anioEmision(2018)
                    .build();
            regulacionRepository.save(reg1);

            Regulacion reg2 = Regulacion.builder()
                    .nombre("Norma de Auditoría Interna")
                    .contenido("Estandares de Auditoría Interna")
                    .urlDocumento("http://gov/norma-aud.pdf")
                    .entidadEmisora("Ministerio de comercio")
                    .anioEmision(2020)
                    .build();
            regulacionRepository.save(reg2);
        }
        if (politicaRepo.count() == 0) {
            PoliticaEmpresa p = PoliticaEmpresa.builder()
                    .empresaId(empresaRepository.findAll().iterator().next().getId())
                    .titulo("Política de Gestión de Riesgos")
                    .contenido("Definición de procesos para identificar, evaliar y mitigar riesgos criticos.")
                    .aiGenerada(false)
                    .estado("approved")
                    .aprobadoPor(usuarioRepository.findAll().iterator().next().getId())
                    .build();
            politicaRepo.save(p);
        }

        if (protocoloRepo.count() == 0) {
            Protocolo protocolo = Protocolo.builder()
                    .nombre("Protocolo de Respuesta a Incidentes")
                    .descripcion("Procedimientos para responder a incidentes de seguridad.")
                    .empresaId(empresaRepository.findAll().iterator().next().getId())
                    .objetivo("Minimizar impacto por incidentes.")
                    .reglas(List.of("Escalar al administrador", "Notificar a cumplimiento"))
                    .build();
            protocoloRepo.save(protocolo);
        }

        if (procedimientoRepo.count() == 0) {
            Protocolo p = protocoloRepo.findAll().iterator().next();
            Procedimiento procedimiento = Procedimiento.builder()
                    .nombre("Manejo de incidentes")
                    .descripcion("Procedimiento operativo para incidentes")
                    .pasos(List.of("Identificar", "Contener", "Remediar"))
                    .objetivo("Gestión de incidentes")
                    .protocoloId(p.getIdProtocolo())
                    .empresaId(p.getEmpresaId())
                    .build();
            procedimientoRepo.save(procedimiento);
        }

        if (auditoriaRepo.count() == 0) {
            Empresa e = empresaRepository.findAll().iterator().next();
            Auditoria auditoria = Auditoria.builder()
                    .empresaId(e.getId())
                    .tipo("Interno")
                    .objetivo("Revisión anual de controles")
                    .alcance("Toda la organización")
                    .auditorLider(usuarioRepository.findAll().iterator().next().getId())
                    .fecha(LocalDateTime.now())
                    .score(88.5)
                    .hallazgosCriticosMsj(List.of("Falla control X"))
                    .hallazgosMayoresMsj(List.of("Mejorar registro Y"))
                    .hallazgosMenoresMsj(List.of("Actualizar control Z"))
                    .recomendaciones("Reforzar controles y capacitaciones")
                    .build();
            auditoriaRepo.save(auditoria);
        }

        if (ollamaRepo.count() == 0) {
            Empresa a = empresaRepository.findAll().iterator().next();
            OllamaResponse response = OllamaResponse.builder()
                    .empresaId(a.getId())
                    .usuarioId(usuarioRepository.findAll().iterator().next().getId())
                    .pregunta("¿Como mejorar la gestión de riesgos?")
                    .respuesta("Sugerencia: Implementar matriz de riesgos y revisiones trimestrales.")
                    .build();
            ollamaRepo.save(response);
        }

        if (riesgoRepo.count() == 0) {
            Empresa e = empresaRepository.findAll().iterator().next();
            Riesgo riesgo = Riesgo.builder()
                    .empresaId(e.getId())
                    .titulo("Riesgo de fuga de datos")
                    .descripcion("Posible fuga por accesos no autorizados.")
                    .categoria("cumplimiento")
                    .probabilidad("alta")
                    .impacto("alto")
                    .nivelRiesgo("alto")
                    .medidasMitigacion("Implementar DLP y Capacitar empleados")
                    .responsable(usuarioRepository.findAll().iterator().next().getId())
                    .estado("abierto")
                    .build();
            riesgoRepo.save(riesgo);
        }
    }

}
