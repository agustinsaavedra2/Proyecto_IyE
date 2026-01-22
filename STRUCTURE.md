Proyecto (estructura completa)
├── README.md
├── BackendIE/
│   ├── .gitattributes
│   ├── .gitignore
│   ├── eror.txt
│   ├── filebeat.yml
│   ├── ml_model.json
│   ├── mvnw
│   ├── mvnw.cmd
│   ├── nginx.conf
│   ├── pom.xml
│   ├── prometheus.yml
│   ├── README_DOCKER.md
│   ├── README.md
│   ├── requirements.txt
│   ├── run_pipeline.ps1
│   ├── valor)
│   ├── archive/
│   │   └── .placeholder
│   ├── backups/
│   │   └── gold_backup_20251213_192752.jsonl
│   ├── docs/
│   │   ├── demo_guion.md
│   │   ├── mail_latency_notes.md
│   │   └── postman_validated_endpoints.md
│   ├── llm_finetune/
│   │   ├── dataset_filtered.jsonl
│   │   ├── pdfs_to_jsonl.py
│   │   ├── README_FINE_TUNE.md
│   │   └── README.md
│   ├── ml/
│   │   ├── __init__.py
│   │   ├── cleanup_report.md
│   │   ├── eval.py
│   │   ├── explain.py
│   │   ├── index.jsonl
│   │   ├── manifest.json
│   │   ├── predict.py
│   │   ├── prepare_dataset.py
│   │   ├── README_CLEANUP.md
│   │   ├── README.md
│   │   ├── requirements.txt
│   │   ├── trainer.py
│   │   ├── __pycache__/
│   │   ├── chunks/
│   │   │   └── Empresas_y_Organizaciones_Sindicales_sancionadas_por_Prácticas_Antisindicales_primer_semestre_2025_table1_row1_chunk01.json
│   │   ├── data/
│   │   │   ├── gold_augmented.jsonl
│   │   │   ├── gold_final.jsonl
│   │   │   ├── gold.jsonl
│   │   │   ├── augment_debug/
│   │   │   ├── csv/
│   │   │   └── jsonl/
│   │   ├── dataset/
│   │   ├── db/
│   │   │   └── schema.sql
│   │   ├── ml-api/
│   │   │   └──ml/
│   │   │   |  └──models/
│   │   │   |      └──manifest.json
│   │   │   |      └──model.joblib
│   │   │   |      └──vectorizer.joblib
│   │   │   ├── app.py
│   │   │   ├── Dockerfile
│   │   │   └── requirements.txt
│   │   ├── models/
│   │   │   ├── manifest.json
│   │   │   ├── model.joblib
│   │   │   └── vectorizer.joblib
│   │   ├── models_augmented/
│   │   │   ├── manifest.json
│   │   │   ├── model.joblib
│   │   │   └── vectorizer.joblib
│   │   ├── scripts/
│   │   │   ├── append_programmatic_until_target.py
│   │   │   ├── archive_unused.py
│   │   │   ├── augment_and_label.py
│   │   │   ├── bootstrap_labels_ollama.py
│   │   │   ├── dedupe_and_prepare_gold.py
│   │   │   ├── extract_tables.py
│   │   │   ├── generate_chunks.py
│   │   │   ├── generate_gold.py
│   │   │   ├── generate_policy.py
│   │   │   ├── gold_to_index.py
│   │   │   ├── import_gold_sqlite.py
│   │   │   └── ... (otros scripts auxiliares)
│   │   ├── service/
│   │   │   ├── app.py
│   │   │   └── ...
│   │   └── tests/
│   ├── ml_storage/
│   ├── ml_training/
│   │   ├── pdf_to_jsonl_filtered.py
│   │   └── send_pdf_email.py
│   ├── reports/
│   │   ├── demo_manual_phase_success.json
│   │   ├── demo_newman_phase1.json
│   │   └── demo_newman_phase2.json
│   ├── scripts/
│   │   ├── BackendIE - Full Demo Collection (validated).postman_collection.json
│   │   ├── demo_local.ps1
│   │   ├── demo.ps1
│   │   ├── pg_backup.sh
│   │   ├── postman_collection_full_auto_demo.json
│   │   ├── postman_collection_phase2.json
│   │   ├── postman_collection.json
│   │   ├── postman_environment.json
│   │   ├── README_BACKUP.md
│   │   ├── run_demo_newman.ps1
│   │   └── run_demo_phases.ps1
│   ├── src/
│   │   ├── main/
│   │   └── test/
│   ├── target/
│   │   ├── classes/
│   │   ├── generated-sources/
│   │   ├── generated-test-sources/
│   │   └── test-classes/
│   └── tests/
│       ├── test_predict_module.py
│       └── test_trainer_smoke.py
├── Frontend_2/
│   ├── .dockerignore
│   ├── .gitignore
│   ├── components.json
│   ├── controllers.txt
│   ├── Dockerfile
│   ├── next.config.mjs
│   ├── package.json
│   ├── pnpm-lock.yaml
│   ├── postcss.config.mjs
│   ├── Problemas.txt
│   ├── README.md
│   ├── tsconfig.json
│   ├── app/
│   │   ├── globals.css
│   │   ├── layout.tsx
│   │   ├── page.tsx
│   │   ├── auditorias/
│   │   │   └── page.tsx
│   │   ├── audits/
│   │   │   └── page.tsx
│   │   ├── auth/
│   │   │   ├── complete/
│   │   │   ├── login/
│   │   │   ├── register/
│   │   │   └── verify/
│   │   ├── categorias/
│   │   │   ├── page.tsx
│   │   │   ├── [id]/
│   │   │   └── crear/
│   │   ├── dashboard/
│   │   │   └── page.tsx
│   │   ├── empresas/
│   │   │   ├── page.tsx
│   │   │   └── registrar/
│   │   ├── ollama/
│   │   │   └── page.tsx
│   │   │   ├── crearAuditoria/
│   │   │   ├── politica/
│   │   │   ├── procedimiento/
│   │   │   └── protocolo/
│   │   ├── planes/
│   │   │   └── crear/
│   │   ├── regulations/
│   │   │   ├── page.tsx
│   │   │   ├── RegulationsClient.tsx
│   │   │   ├── [id]/
│   │   │   └── new/
│   │   ├── risks/
│   │   │   └── page.tsx
│   │   ├── suscripcion/
│   │   │   └── page.tsx
│   │   └── users/
│   │       └── page.tsx
│   ├── components/
│   │   ├── theme-provider.tsx
│   │   ├── audits/
│   │   │   └── audits-list.tsx
│   │   ├── auth/
│   │   │   └── register-form.tsx
│   │   ├── dashboard/
│   │   │   ├── dashboard-nav.tsx
│   │   │   ├── quick-actions.tsx
│   │   │   ├── recent-activity.tsx
│   │   │   └── stats-cards.tsx
│   │   ├── regulations/
│   │   │   └── regulations-list.tsx
│   │   ├── risks/
│   │   │   └── risks-list.tsx
│   │   └── ui/
│   │       ├── accordion.tsx
│   │       ├── alert-dialog.tsx
│   │       ├── alert.tsx
│   │       ├── aspect-ratio.tsx
│   │       ├── avatar.tsx
│   │       ├── back-button.tsx
│   │       ├── badge.tsx
│   │       ├── breadcrumb.tsx
│   │       ├── button-group.tsx
│   │       ├── button.tsx
│   │       ├── calendar.tsx
│   │       ├── card.tsx
│   │       ├── carousel.tsx
│   │       ├── chart.tsx
│   │       ├── checkbox.tsx
│   │       ├── collapsible.tsx
│   │       ├── command.tsx
│   │       ├── context-menu.tsx
│   │       ├── dialog.tsx
│   │       ├── draft-back-button.tsx
│   │       ├── drawer.tsx
│   │       ├── dropdown-menu.tsx
│   │       ├── empty.tsx
│   │       ├── field.tsx
│   │       ├── form-dirty-alert.tsx
│   │       ├── form.tsx
│   │       ├── hover-card.tsx
│   │       ├── input-group.tsx
│   │       ├── input-otp.tsx
│   │       ├── input.tsx
│   │       ├── item.tsx
│   │       ├── kbd.tsx
│   │       ├── label.tsx
│   │       ├── loading-overlay.tsx
│   │       ├── loading-skeleton.tsx
│   │       ├── menubar.tsx
│   │       ├── navigation-menu.tsx
│   │       ├── pagination.tsx
│   │       ├── popover.tsx
│   │       └── ... (otros componentes UI)
│   ├── hooks/
│   │   ├── use-mobile.ts
│   │   └── use-toast.ts
│   ├── lib/
│   │   ├── apis.ts
│   │   ├── auditoriaService.ts
│   │   ├── categoriaService.ts
│   │   ├── empresaService.ts
│   │   ├── ollamaResponseService.ts
│   │   ├── planService.ts
│   │   ├── politicaService.ts
│   │   ├── procedimientoService.ts
│   │   ├── protocoloService.ts
│   │   ├── regulationService.ts
│   │   ├── riesgoService.ts
│   │   ├── suscripcionService.ts
│   │   ├── userService.ts
│   │   └── utils.ts
│   ├── public/
│   ├── styles/
│   │   └── globals.css
│   └── types/
│       ├── auditoria.ts
│       ├── auth.ts
│       ├── categoria.ts
│       ├── empresa.ts
│       ├── ollama.ts
│       ├── plan.ts
│       ├── politica.ts
│       ├── procedimiento.ts
│       ├── protocolo.ts
│       ├── regulacion.ts
│       ├── riesgo.ts
│       └── suscripcion.ts
├── docker-compose.yml
└── (fin de proyecto)
