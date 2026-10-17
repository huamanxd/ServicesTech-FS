-- Referencias en README: precios públicos consultados en octubre de 2026.
-- Mano de obra estimada; no incluir licencias, repuestos ni desplazamiento salvo indicación.
INSERT INTO servicios (codigo,nombre,descripcion,categoria,precio_referencial,duracion_minutos) VALUES
('DIAG-01','Diagnóstico de PC o laptop','Evaluación de fallas y diagnóstico técnico con presupuesto previo.', 'EQUIPOS', 35.00, 45),
('MANT-01','Mantenimiento preventivo','Limpieza interna, pasta térmica y revisión de rendimiento de PC o laptop convencional.', 'EQUIPOS', 95.00, 120),
('MANT-02','Mantenimiento gaming','Limpieza avanzada, evaluación térmica de equipo gaming y optimización.', 'EQUIPOS', 190.00, 150),
('SOFT-01','Formateo e instalación de sistema','Reinstalación del sistema operativo y controladores; licencia no incluida.', 'SOFTWARE', 95.00, 150),
('SOFT-02','Respaldo, Windows y configuración','Copia preventiva, reinstalación y configuración inicial; licencias no incluidas.', 'SOFTWARE', 150.00, 180),
('SOFT-03','Soporte técnico remoto','Asistencia a distancia para fallas habituales y configuración de software; por sesión.', 'SOFTWARE', 60.00, 60),
('HARD-01','Instalación de SSD y migración','Mano de obra de instalación y transferencia de información; SSD no incluido.', 'EQUIPOS', 120.00, 120),
('DATA-01','Recuperación inicial de datos','Evaluación y recuperación lógica básica; casos físicos complejos sujetos a cotización.', 'SOFTWARE', 200.00, 180),
('RED-01','Optimización de red Wi-Fi','Diagnóstico de cobertura, ajustes del router y configuración de conectividad.', 'REDES', 95.00, 90),
('RED-02','Configuración de red para oficina','Configuración básica de conectividad y recursos compartidos, por visita.', 'REDES', 250.00, 180),
('CCTV-01','Instalación de cámara de seguridad','Mano de obra por cámara; cámara, cableado especial y equipos no incluidos.', 'SEGURIDAD', 80.00, 90),
('EMPR-01','Visita de soporte para empresa','Atención presencial y evaluación inicial de equipos y conectividad; por visita.', 'EMPRESAS', 120.00, 90);
