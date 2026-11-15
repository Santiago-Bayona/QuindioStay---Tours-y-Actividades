"""Genera 02_DML_GrupoNN.sql (uso: python generar_datos.py, requiere pip install bcrypt) con datos verosímiles y asimétricos que cumplen todas
las reglas de negocio de QuindíoStay - Tours y Actividades.

Semilla fija: cada ejecución produce exactamente el mismo script.
"""
import random
from datetime import date, datetime, time, timedelta
from decimal import Decimal

import bcrypt

random.seed(20261115)
HOY = datetime(2026, 11, 15, 0, 0)          # fecha de corte de los datos
INICIO = date(2025, 1, 1)
FIN = date(2026, 12, 31)

# ---------------------------------------------------------------------------- utilidades


def q(v):
    """Valor SQL."""
    if v is None:
        return "NULL"
    if isinstance(v, bool):
        return "TRUE" if v else "FALSE"
    if isinstance(v, (int, Decimal)):
        return str(v)
    if isinstance(v, datetime):
        return "'" + v.strftime("%Y-%m-%d %H:%M:%S") + "'"
    if isinstance(v, date):
        return "'" + v.isoformat() + "'"
    if isinstance(v, time):
        return "'" + v.strftime("%H:%M:%S") + "'"
    return "'" + str(v).replace("'", "''") + "'"


def insert(tabla, columnas, filas, por_bloque=60):
    out = []
    for i in range(0, len(filas), por_bloque):
        bloque = filas[i:i + por_bloque]
        out.append(f"INSERT INTO {tabla} ({', '.join(columnas)}) VALUES\n" +
                   ",\n".join("  (" + ", ".join(q(v) for v in f) + ")" for f in bloque) + ";")
    return "\n".join(out)


def edad(nacimiento, en_fecha):
    return en_fecha.year - nacimiento.year - ((en_fecha.month, en_fecha.day) < (nacimiento.month, nacimiento.day))


def fecha_aleatoria(desde, hasta):
    return desde + timedelta(days=random.randint(0, (hasta - desde).days))


telefonos_usados = set()


def telefono():
    while True:
        t = "3" + random.choice(["00", "01", "02", "04", "05", "10", "11", "12", "13", "14", "15", "16", "17",
                                 "18", "20", "21", "22", "23", "24", "50"]) + f"{random.randint(0, 9999999):07d}"
        if t not in telefonos_usados:
            telefonos_usados.add(t)
            return t


# Contraseña de prueba de todas las cuentas: Clave123*  (BCrypt, prefijo $2a$ como Spring)
def hash_clave():
    return bcrypt.hashpw(b"Clave123*", bcrypt.gensalt(rounds=10, prefix=b"2a")).decode()


# ---------------------------------------------------------------------------- catálogos
MUNICIPIOS = [(1, "Armenia", "63001"), (2, "Buenavista", "63111"), (3, "Calarcá", "63130"),
              (4, "Circasia", "63190"), (5, "Córdoba", "63212"), (6, "Filandia", "63272"),
              (7, "Génova", "63302"), (8, "La Tebaida", "63401"), (9, "Montenegro", "63470"),
              (10, "Pijao", "63548"), (11, "Quimbaya", "63594"), (12, "Salento", "63690")]
TIPOS = [(1, "Alojamiento"), (2, "Agencia de viajes"), (3, "Transporte"), (4, "Gastronomía"),
         (5, "Guianza"), (6, "Alquiler de vehículos"), (7, "Atracción"), (8, "Otros")]
ROLES = [(1, "Administrador de la plataforma"), (2, "Encargado de prestador"), (3, "Operador")]
CATEGORIAS = {1: "Cultural", 2: "Aventura", 3: "Naturaleza", 4: "Cafetero"}
IDIOMAS = {1: "Español", 2: "Inglés", 3: "Francés", 4: "Alemán", 5: "Portugués"}

# ---------------------------------------------------------------------------- prestadores
# (id, nombre, nit, rnt, dirección, municipio, tipo, estado, correo)
PRESTADORES = [
    (1, "Cocora Expediciones SAS", "900123456-1", "45871", "Calle 6 # 4-15", 12, 5, "ACTIVO", "contacto@cocoraexpediciones.co"),
    (2, "Aventura Quindío Tours", "901234567-2", "51234", "Vía al mirador km 2", 2, 2, "ACTIVO", "reservas@aventuraquindio.co"),
    (3, "Ruta del Café Filandia", "900987654-3", "38765", "Carrera 5 # 7-20", 6, 5, "ACTIVO", "hola@rutadelcafe.co"),
    (4, "Salento Trekking y Bici", "901456789-4", "52310", "Carrera 4 # 2-38", 12, 5, "ACTIVO", "info@salentotrekking.co"),
    (5, "Parapente Alas del Quindío", "901567890-5", "49877", "Mirador de Buenavista, vereda La Granja", 2, 7, "ACTIVO", "vuelos@alasdelquindio.co"),
    (6, "Finca La Palmera Café Tour", "900765432-6", "40112", "Vereda Palestina km 3", 12, 7, "ACTIVO", "visitas@fincalapalmera.co"),
    (7, "Aves de Génova Birding", "901678901-7", "53498", "Calle 25 # 12-10", 7, 5, "ACTIVO", "birding@avesdegenova.co"),
    (8, "Armenia a Pie Recorridos", "901789012-8", "46620", "Carrera 14 # 21-33", 1, 2, "ACTIVO", "recorridos@armeniaapie.co"),
    (9, "Rafting Río Barragán", "900345678-9", "41987", "Vía Pijao - Génova km 8", 10, 7, "SUSPENDIDO", "rafting@riobarragan.co"),
    (10, "Senderos de Pijao", "901778899-6", "41122", "Plaza principal", 10, 5, "RETIRADO", "senderos@pijao.co"),
    (11, "Quimbaya Ancestral Tours", "901890123-1", "54761", "Calle 15 # 6-22", 11, 2, "ACTIVO", "tours@quimbayaancestral.co"),
    (12, "Montenegro Cafetero Experiencias", "901901234-2", "55012", "Vía Montenegro - Pueblo Tapao km 2", 9, 7, "ACTIVO", "experiencias@montenegrocafetero.co"),
    (13, "Hotel Mirador del Valle", "901112223-4", "60011", "Avenida Bolívar # 14N-30", 1, 1, "ACTIVO", "reservas@miradordelvalle.co"),
    (14, "Hostal Calle Real", "901223344-5", "60457", "Calle Real # 5-12", 12, 1, "ACTIVO", "hostal@callereal.co"),
    (15, "Transportes Eje Turístico", "900445566-5", "29870", "Calle 40 # 25-10", 3, 3, "SUSPENDIDO", "info@ejeturistico.co"),
    (16, "Jeeps del Cocora", "901334455-6", "61208", "Plaza de Bolívar, costado sur", 12, 3, "ACTIVO", "viajes@jeepsdelcocora.co"),
    (17, "Restaurante La Fonda Paisa", "901445566-7", "62014", "Carrera 6 # 8-14", 6, 4, "ACTIVO", "fonda@lafondapaisa.co"),
    (18, "Trucha y Café Circasia", "901556677-8", "62890", "Vía Circasia - Montenegro km 1", 4, 4, "ACTIVO", "pedidos@truchaycafe.co"),
    (19, "Rent a Car Café", "901667788-9", "63355", "Calle 21 # 19-40", 1, 6, "ACTIVO", "alquiler@rentacarcafe.co"),
    (20, "Glamping Valle Verde", "901778800-1", "64120", "Vereda El Alambrado", 8, 1, "ACTIVO", "glamping@valleverde.co"),
]
OPERADORES_ACTIVOS = {p[0] for p in PRESTADORES if p[7] == "ACTIVO"}

# ---------------------------------------------------------------------------- tours
# (id, prestador, nombre, descripción, horas, dificultad, edad mín, precio, categoría,
#  popularidad, horas de salida, cupos posibles, periodo (desde, hasta) o None)
TOURS = [
    (1, 1, "Caminata Valle de Cocora", "Recorrido entre palmas de cera hasta Acaime, cruzando puentes colgantes sobre el río Quindío. Incluye hidratación y seguro.", "5.00", "MEDIA", 8, 120000, 3, 10, ["07:00", "08:00"], [15, 18, 20], None),
    (2, 1, "Cabalgata por el Valle de Cocora", "Paseo a caballo por senderos del valle con guía baqueano y parada en mirador de palmas.", "3.00", "MEDIA", 12, 150000, 3, 4, ["09:00", "14:00"], [8, 10], None),
    (3, 1, "Avistamiento de aves en Acaime", "Observación de colibríes y aves endémicas en la reserva de Acaime, con binoculares.", "4.00", "BAJA", 0, 95000, 3, 3, ["06:00"], [8, 10], None),
    (4, 2, "Canopy y rappel en cascada", "Circuito de canopy de cinco líneas y descenso en rappel por una cascada de 30 metros.", "3.00", "ALTA", 12, 180000, 2, 5, ["09:00", "13:00"], [10, 12], None),
    (5, 2, "Torrentismo en Buenavista", "Descenso por el cauce de una quebrada con cuerdas y equipo certificado.", "3.50", "ALTA", 14, 160000, 2, 2, ["09:00"], [8], None),
    (6, 3, "Tour del café en finca tradicional", "Del grano a la taza: recolección, beneficio, tostión y catación en una finca familiar.", "3.50", "BAJA", 0, 85000, 4, 8, ["09:00", "14:00"], [15, 20], None),
    (7, 3, "Recorrido histórico por Filandia", "Arquitectura de la colonización antioqueña, calle del Tiempo Detenido y mirador.", "2.50", "BAJA", 0, 60000, 1, 3, ["10:00", "16:00"], [20, 25], None),
    (8, 3, "Taller de cestería en Filandia", "Taller práctico con artesanos locales sobre el tejido de canastos en bejuco.", "2.00", "BAJA", 6, 70000, 1, 0, ["10:00"], [12], "SIN_SALIDAS"),
    (9, 4, "Ruta en bicicleta Salento - Boquía", "Descenso en bicicleta todoterreno por la vía antigua hasta Boquía y regreso en jeep.", "4.00", "MEDIA", 14, 130000, 2, 3, ["08:00"], [10, 12], None),
    (10, 4, "Trekking al Paramillo del Quindío", "Ascenso de alta montaña por el páramo, con noche previa de aclimatación no incluida.", "10.00", "ALTA", 16, 380000, 3, 1, ["05:00"], [8], None),
    (11, 5, "Parapente biplaza en Buenavista", "Vuelo biplaza sobre el valle del río Barragán con piloto certificado y video incluido.", "1.50", "ALTA", 14, 250000, 2, 7, ["09:00", "11:00", "14:00"], [6], None),
    (12, 6, "Cata de cafés especiales", "Cata guiada de cuatro cafés especiales de Salento con maridaje.", "2.00", "BAJA", 0, 75000, 4, 4, ["10:00", "15:00"], [12, 15], None),
    (13, 6, "Del grano a la taza en Salento", "Recorrido por cultivos, despulpado, secado y preparación de bebidas de café.", "3.00", "BAJA", 0, 90000, 4, 5, ["09:00"], [15, 20], None),
    (14, 7, "Avistamiento de aves en Génova", "Jornada de observación en bosque andino; posibilidad de ver barranquero y tángaras.", "6.00", "MEDIA", 10, 140000, 3, 2, ["05:30"], [8], None),
    (15, 8, "Armenia cultural y Museo del Oro Quimbaya", "Recorrido por el centro, la plaza de Bolívar y el museo de la cultura quimbaya.", "3.00", "BAJA", 0, 55000, 1, 2, ["09:00", "14:00"], [20, 25], None),
    (16, 8, "Ruta gastronómica de Armenia", "Degustación de platos típicos del Quindío en plazas y restaurantes tradicionales.", "3.00", "BAJA", 0, 98000, 1, 2, ["11:00"], [15], None),
    (17, 9, "Rafting en el río Barragán", "Descenso en balsa por rápidos clase III con instructores y equipo de seguridad.", "4.00", "ALTA", 14, 170000, 2, 3, ["09:00"], [10], ("2025-01-01", "2026-05-31")),
    (18, 10, "Pijao pueblo sin prisa", "Recorrido por el primer pueblo Cittaslow de Colombia: plaza, balcones y cafés.", "3.00", "BAJA", 0, 65000, 1, 2, ["10:00"], [15], ("2025-01-01", "2025-09-30")),
    (19, 10, "Caminata a la cascada de Pijao", "Sendero de montaña hasta una cascada en la cordillera Central.", "4.00", "MEDIA", 10, 80000, 3, 2, ["08:00"], [12], ("2025-01-01", "2025-09-30")),
    (20, 11, "Noche de velas en Quimbaya", "Recorrido nocturno por los barrios decorados con faroles y velas en temporada de diciembre.", "2.00", "BAJA", 0, 50000, 1, 6, ["18:30"], [25, 30], "DICIEMBRE"),
    (21, 11, "Tour arqueológico quimbaya", "Visita a sitios con hallazgos de la cultura quimbaya y taller de réplicas en arcilla.", "3.00", "BAJA", 8, 70000, 1, 0, ["10:00"], [15], "SOLO_FUTURAS"),
    (22, 12, "Paseo en Willys por fincas cafeteras", "Recorrido en jeep Willys por fincas de Montenegro con almuerzo campesino.", "4.00", "BAJA", 0, 110000, 4, 4, ["09:00"], [12], None),
]
TOUR = {t[0]: t for t in TOURS}

# ---------------------------------------------------------------------------- planes (40)
# (id, prestador, nombre, descripción, tipo, precio, inicio, fin, estado)
PLANES = []


def plan(prest, nombre, desc, tipo, precio, ini, fin, estado="ACTIVO"):
    PLANES.append((len(PLANES) + 1, prest, nombre, desc, tipo, precio, date.fromisoformat(ini), date.fromisoformat(fin), estado))


for p in (1, 2, 3, 4, 5, 6, 7, 8, 11, 12):
    plan(p, "Grupal temporada 2025", "Salida compartida con otros viajeros, guía certificado y seguro.", "ESTANDAR", 100000, "2025-01-01", "2025-12-31")
    plan(p, "Grupal 2026", "Salida compartida con otros viajeros, guía certificado y seguro.", "ESTANDAR", 110000, "2026-01-01", "2026-12-31")
for p in (1, 2, 4, 5, 6):
    plan(p, "Privado", "Salida exclusiva para el grupo, con transporte desde el casco urbano.", "PREMIUM", 280000, "2025-01-01", "2026-12-31")
for p in (1, 3, 12):
    plan(p, "Todo incluido", "Transporte ida y regreso, almuerzo típico, fotos y seguro.", "TODO_INCLUIDO", 320000, "2025-03-01", "2026-12-31")
plan(3, "Básico sin transporte", "Solo la actividad; el viajero llega por su cuenta.", "BASICO", 60000, "2025-01-01", "2026-12-31")
plan(9, "Grupal temporada 2025", "Descenso compartido con instructor.", "ESTANDAR", 150000, "2025-01-01", "2025-12-31")
plan(9, "Grupal 2026", "Descenso compartido con instructor.", "ESTANDAR", 160000, "2026-01-01", "2026-12-31", "INACTIVO")
plan(10, "Grupal 2025", "Recorrido compartido.", "BASICO", 60000, "2025-01-01", "2025-12-31", "INACTIVO")
plan(10, "Privado 2025", "Recorrido exclusivo para el grupo.", "PREMIUM", 180000, "2025-01-01", "2025-12-31", "INACTIVO")
plan(13, "Solo alojamiento", "Habitación doble sin alimentación.", "BASICO", 250000, "2025-01-01", "2026-12-31")
plan(13, "Con desayuno", "Habitación doble con desayuno típico.", "ESTANDAR", 290000, "2025-01-01", "2026-12-31")
plan(14, "Cama en habitación compartida", "Cama en habitación de seis con baño compartido.", "BASICO", 70000, "2025-01-01", "2026-12-31")
plan(16, "Trayecto Salento - Cocora", "Puesto en jeep Willys ida y regreso.", "BASICO", 18000, "2025-01-01", "2026-12-31")
plan(17, "Menú del día", "Bandeja paisa o trucha con bebida.", "BASICO", 35000, "2024-06-01", "2025-06-30", "INACTIVO")
plan(19, "Alquiler por día", "Camioneta 4x4 con seguro todo riesgo.", "ESTANDAR", 260000, "2025-01-01", "2026-12-31")
plan(20, "Noche en domo", "Domo con jacuzzi y desayuno.", "PREMIUM", 420000, "2025-01-01", "2026-12-31")
assert len(PLANES) == 40, len(PLANES)
PLANES_POR_PRESTADOR = {}
for pl in PLANES:
    PLANES_POR_PRESTADOR.setdefault(pl[1], []).append(pl)

# ---------------------------------------------------------------------------- guías
# (id, nombre, documento, tarjeta, idiomas, carga relativa)
GUIAS = [
    (1, "Juan Pablo Ramírez", "1094888111", "TP-QUI-0101", [1, 2], 10),
    (2, "María Fernanda Ospina", "1094777222", "TP-QUI-0102", [1, 2, 3], 8),
    (3, "Carlos Andrés Gómez", "1097333444", "TP-QUI-0103", [1], 9),
    (4, "Laura Valentina Arias", "1094555666", "TP-QUI-0104", [1, 2, 4], 6),
    (5, "Andrés Felipe Restrepo", "1094999000", "TP-QUI-0105", [1, 5], 5),
    (6, "Diana Marcela Cardona", "1094321654", "TP-QUI-0106", [1, 2], 7),
    (7, "Jhon Jairo Henao", "9727654", "TP-QUI-0107", [1], 6),
    (8, "Paula Andrea Giraldo", "1094456789", "TP-QUI-0108", [1, 2, 5], 4),
    (9, "Sebastián Londoño", "1094112233", "TP-QUI-0109", [1], 3),
    (10, "Natalia Quintero", "1094665544", "TP-QUI-0110", [1, 3], 2),
    (11, "Mateo Valencia", "1094778899", "TP-QUI-0111", [1, 2], 2),
    (12, "Luisa Fernanda Zapata", "1094990011", "TP-QUI-0112", [1, 4], 1),
    (13, "Héctor Fabio Salazar", "7553210", "TP-QUI-0113", [1], 1),
    (14, "Camila Andrea Muñoz", "1094223344", "TP-QUI-0114", [1, 2], 1),
]

# ---------------------------------------------------------------------------- clientes (150)
NOMBRES_M = ["Santiago", "Mateo", "Sebastián", "Alejandro", "Samuel", "Nicolás", "Daniel", "Juan Esteban", "David",
             "Andrés", "Felipe", "Julián", "Camilo", "Diego", "Juan Pablo", "Carlos", "Jorge", "Luis Fernando",
             "Esteban", "Tomás", "Emilio", "Simón", "Gabriel", "Óscar", "Ricardo", "Mauricio", "Hernán", "Jaime"]
NOMBRES_F = ["Valentina", "Mariana", "Sofía", "Isabella", "Daniela", "Camila", "Gabriela", "Luciana", "Sara",
             "Laura", "María José", "Ana María", "Paula", "Natalia", "Juliana", "Manuela", "Carolina", "Andrea",
             "Catalina", "Alejandra", "Lina", "Diana", "Paola", "Melissa", "Ximena", "Luisa", "Beatriz", "Gloria"]
APELLIDOS = ["Rojas", "Herrera", "Duque", "Gómez", "Rodríguez", "López", "Martínez", "García", "Ramírez", "Cardona",
             "Giraldo", "Osorio", "Ospina", "Restrepo", "Valencia", "Zuluaga", "Arango", "Castaño", "Quintero",
             "Henao", "Montoya", "Londoño", "Salazar", "Mejía", "Correa", "Vargas", "Torres", "Muñoz", "Ríos",
             "Álvarez", "Parra", "Patiño", "Bedoya", "Toro", "Echeverri", "Jaramillo", "Marín", "Agudelo"]
CIUDADES = (["Bogotá"] * 22 + ["Medellín"] * 12 + ["Cali"] * 10 + ["Pereira"] * 7 + ["Manizales"] * 5 +
            ["Armenia"] * 5 + ["Bucaramanga"] * 3 + ["Barranquilla"] * 3 + ["Ibagué"] * 3 + ["Cartagena"] * 2 +
            ["Neiva", "Villavicencio", "Pasto", "Santa Marta", "Tunja", "Popayán", "Cúcuta", "Montería"])
EXTRANJEROS = [  # (nombre, apellido, ciudad, idioma, tipo doc)
    ("Emma", "Schneider", "Múnich", "INGLES", "PASAPORTE"), ("Lucas", "Martin", "Lyon", "INGLES", "PASAPORTE"),
    ("João", "Silva", "São Paulo", "INGLES", "PASAPORTE"), ("Olivia", "Smith", "Toronto", "INGLES", "PASAPORTE"),
    ("Noah", "Johnson", "Austin", "INGLES", "PASAPORTE"), ("Chloé", "Dubois", "París", "INGLES", "PASAPORTE"),
    ("Lukas", "Weber", "Berlín", "INGLES", "PASAPORTE"), ("Sophie", "de Vries", "Ámsterdam", "INGLES", "PASAPORTE"),
    ("James", "Taylor", "Londres", "INGLES", "PASAPORTE"), ("Lucía", "Fernández", "Madrid", "ESPANOL", "PASAPORTE"),
    ("Martín", "González", "Buenos Aires", "ESPANOL", "CEDULA_EXTRANJERIA"), ("Fernanda", "Rocha", "Río de Janeiro", "INGLES", "PASAPORTE"),
    ("Diego", "Hernández", "Ciudad de México", "ESPANOL", "PASAPORTE"), ("Valeria", "Muñoz", "Santiago de Chile", "ESPANOL", "CEDULA_EXTRANJERIA"),
    ("Ethan", "Brown", "Chicago", "INGLES", "PASAPORTE"), ("Anna", "Müller", "Zúrich", "INGLES", "PASAPORTE"),
    ("Pedro", "Almeida", "Lisboa", "INGLES", "PASAPORTE"), ("Gabriel", "Pérez", "Caracas", "ESPANOL", "CEDULA_EXTRANJERIA"),
    ("Isabel", "Castro", "Quito", "ESPANOL", "PASAPORTE"), ("Thomas", "Lefèvre", "Montreal", "INGLES", "PASAPORTE"),
]


def quitar_tildes(s):
    for a, b in zip("áéíóúÁÉÍÓÚñÑüèç", "aeiouAEIOUnNuec"):
        s = s.replace(a, b)
    return s


correos_usados = set()
documentos_usados = set()


def correo(nombre, apellido):
    base = quitar_tildes(nombre.split()[0] + "." + apellido.split()[0]).lower().replace(" ", "")
    dominio = random.choice(["gmail.com"] * 6 + ["hotmail.com"] * 2 + ["outlook.com", "yahoo.com"])
    c = f"{base}@{dominio}"
    n = 2
    while c in correos_usados:
        c = f"{base}{n}@{dominio}"
        n += 1
    correos_usados.add(c)
    return c


def documento_cc():
    while True:
        d = str(random.choice([random.randint(1000000000, 1130000000), random.randint(10000000, 99999999)]))
        if d not in documentos_usados:
            documentos_usados.add(d)
            return d


def documento_extranjero(tipo):
    while True:
        d = (random.choice("ABCDEFGHKLMNPRSTXYZ") + random.choice("ABCDEFGHKLMNPRSTXYZ") + str(random.randint(1000000, 9999999))
             if tipo == "PASAPORTE" else str(random.randint(100000, 999999)))
        if d not in documentos_usados:
            documentos_usados.add(d)
            return d


CLIENTES = []   # dict
PERFILES = {}   # id_cliente -> dict


def nuevo_cliente(nombre, apellido, tipo_doc, doc, ciudad, nacimiento, correo_c, con_perfil, idioma="ESPANOL",
                  canal=None, anticipacion=None, promos=None):
    cid = len(CLIENTES) + 1
    CLIENTES.append(dict(id=cid, nombre=nombre, apellido=apellido, tipo=tipo_doc, doc=doc, correo=correo_c,
                         tel=telefono(), nac=nacimiento, ciudad=ciudad, peso=random.choice([1, 1, 1, 1, 2, 2, 3, 5])))
    if con_perfil:
        PERFILES[cid] = dict(correo=correo_c, idioma=idioma,
                             canal=canal or random.choice(["CORREO"] * 5 + ["WHATSAPP"] * 4 + ["SMS"] * 2),
                             anticipacion=anticipacion or random.choice([1, 1, 3, 3, 3, 7]),
                             promos=random.random() < 0.55 if promos is None else promos)
    return cid


# Cuentas fijas para las pruebas de login (ver comentario en el script)
documentos_usados.update(["1094123456"])
correos_usados.update(["valentina.rojas@gmail.com"])
nuevo_cliente("Valentina", "Rojas", "CEDULA_CIUDADANIA", "1094123456", "Bogotá", date(1998, 4, 12),
              "valentina.rojas@gmail.com", True, canal="WHATSAPP", anticipacion=3, promos=True)
for nom, ape, ciudad, idioma, tipo in EXTRANJEROS:
    doc = documento_extranjero(tipo)
    nuevo_cliente(nom, ape, tipo, doc, ciudad, fecha_aleatoria(date(1962, 1, 1), date(2003, 12, 31)),
                  correo(nom, ape), random.random() < 0.9, idioma=idioma)
while len(CLIENTES) < 150:
    fem = random.random() < 0.52
    nom = random.choice(NOMBRES_F if fem else NOMBRES_M)
    ape = random.choice(APELLIDOS) + " " + random.choice(APELLIDOS)
    nuevo_cliente(nom, ape, "CEDULA_CIUDADANIA", documento_cc(), random.choice(CIUDADES),
                  fecha_aleatoria(date(1955, 1, 1), date(2006, 6, 30)), correo(nom, ape), random.random() < 0.84)
CLIENTE = {c["id"]: c for c in CLIENTES}

# ============================================================================ salidas


def temporada(d):
    """Factor de demanda: temporada alta en vacaciones de fin de año, Semana Santa,
    mitad de año y receso de octubre; temporada baja en febrero y septiembre."""
    if (d.month == 12 and d.day >= 12) or (d.month == 1 and d.day <= 15):
        return 2.4
    if date(2025, 4, 12) <= d <= date(2025, 4, 20) or date(2026, 3, 28) <= d <= date(2026, 4, 5):
        return 2.2
    if (d.month == 6 and d.day >= 15) or d.month == 7:
        return 1.8
    if date(2025, 10, 4) <= d <= date(2025, 10, 13) or date(2026, 10, 3) <= d <= date(2026, 10, 12):
        return 1.6
    if d.month in (2, 9):
        return 0.6
    if d.weekday() >= 5:
        return 1.3
    return 1.0


SALIDAS = []
ocupado_guia = {}   # (id_guia, fecha) -> True: un guía hace una sola salida por día (sin cruces)


def elegir_guia(fecha):
    idioma = 1 if random.random() < 0.72 else random.choices([2, 3, 4, 5], [60, 15, 15, 10])[0]
    for intento in (idioma, 1):
        candidatos = [g for g in GUIAS if intento in g[4] and (g[0], fecha) not in ocupado_guia]
        if candidatos:
            g = random.choices(candidatos, [c[5] for c in candidatos])[0]
            ocupado_guia[(g[0], fecha)] = True
            return g[0], intento
    return None, None


def nueva_salida(tour, fecha, hora=None):
    gid, iid = elegir_guia(fecha)
    if gid is None:
        return
    hora = hora or random.choice(tour[10])
    if fecha < HOY.date():
        estado = "CANCELADA" if random.random() < 0.035 else "REALIZADA"
    else:
        estado = "PROGRAMADA"
    SALIDAS.append(dict(id=len(SALIDAS) + 1, tour=tour[0], fecha=fecha,
                        hora=time.fromisoformat(hora if len(hora) == 5 else hora), cupo=random.choice(tour[11]),
                        estado=estado, guia=gid, idioma=iid))


d = INICIO
fechas = []
while d <= FIN:
    fechas.append(d)
    d += timedelta(days=1)

for t in TOURS:
    periodo = t[12]
    if periodo == "SIN_SALIDAS":
        continue
    if periodo == "SOLO_FUTURAS":
        nueva_salida(t, date(2026, 12, 12))
        nueva_salida(t, date(2026, 12, 19))
        continue
    if periodo == "DICIEMBRE":
        for f in fechas:
            if f.month == 12 and f.day <= 24 and random.random() < 0.4:
                nueva_salida(t, f)
        continue
    desde, hasta = (INICIO, FIN) if periodo is None else (date.fromisoformat(periodo[0]), date.fromisoformat(periodo[1]))
    for f in fechas:
        if desde <= f <= hasta and random.random() < t[9] * 0.0052 * temporada(f):
            nueva_salida(t, f)

SALIDAS.sort(key=lambda s: (s["fecha"], s["hora"], s["tour"]))
for i, s in enumerate(SALIDAS, 1):
    s["id"] = i
# Una salida futura cancelada por el operador (lluvias), para mostrar la regla del 100 %
futura_cancelada = next(s for s in SALIDAS if s["tour"] == 1 and s["fecha"] >= date(2026, 11, 18))
futura_cancelada["estado"] = "CANCELADA"
for s in SALIDAS:
    inicio = datetime.combine(s["fecha"], s["hora"])
    if s["estado"] == "CANCELADA":
        s["cancelada_en"] = (datetime(2026, 11, 12, 16, 20) if s is futura_cancelada
                             else inicio - timedelta(hours=random.randint(20, 70)))
SALIDA = {s["id"]: s for s in SALIDAS}

# ============================================================================ reservas
INACTIVADO_EN = {}   # plan -> fecha en que se inactivó (antes se pudo vender)
for pl in PLANES:
    if pl[8] == "INACTIVO":
        INACTIVADO_EN[pl[0]] = {9: date(2026, 6, 1), 10: date(2025, 10, 1)}.get(pl[1], pl[7])

PESO_PLAN = {"ESTANDAR": 70, "PREMIUM": 18, "TODO_INCLUIDO": 9, "BASICO": 10}
METODOS = {"WEB": (["TARJETA_CREDITO", "PSE", "TARJETA_DEBITO"], [45, 35, 20]),
           "APP_MOVIL": (["TARJETA_CREDITO", "PSE", "TARJETA_DEBITO"], [50, 30, 20]),
           "TELEFONO": (["TRANSFERENCIA", "PSE", "TARJETA_CREDITO"], [50, 30, 20]),
           "MOSTRADOR": (["EFECTIVO", "TARJETA_DEBITO", "TARJETA_CREDITO"], [60, 25, 15]),
           "AGENCIA_INTERMEDIARIA": (["TRANSFERENCIA", "PSE"], [80, 20])}

RESERVAS, PARTICIPANTES, PAGOS = [], [], []
inscritos = {}       # id_salida -> set(documentos en reservas activas)
clientes_salida = {}  # id_salida -> set(id_cliente)
ocupados = {}        # id_salida -> participantes activos
referencias = {"TXN": 0, "PSE": 0}


def referencia(metodo, fecha):
    if metodo in ("TARJETA_CREDITO", "TARJETA_DEBITO"):
        referencias["TXN"] += 1
        return f"TXN-{fecha.year}-{referencias['TXN']:05d}"
    if metodo == "PSE":
        referencias["PSE"] += 1
        return f"PSE-{fecha.year}-{referencias['PSE']:05d}"
    return None


def pagar(reserva, monto, fecha, estado, metodo=None):
    metodo = metodo or random.choices(*METODOS[reserva["canal"]])[0]
    fecha = min(fecha, HOY - timedelta(minutes=random.randint(5, 600)))
    PAGOS.append(dict(id=len(PAGOS) + 1, fecha=fecha, monto=Decimal(monto), metodo=metodo, estado=estado,
                      ref=referencia(metodo, fecha), reserva=reserva["id"]))
    return PAGOS[-1]


def acompanante(cliente, fecha_salida, edad_min, puede_nino):
    familia = random.random() < 0.55
    nino = familia and puede_nino and random.random() < 0.45
    if nino:
        anos = random.randint(max(edad_min, 3), 17)
    else:
        anos = random.randint(max(edad_min, 18), 68)
    nac = fecha_salida - timedelta(days=anos * 365 + random.randint(20, 340))
    if edad(nac, fecha_salida) < edad_min:
        nac -= timedelta(days=365)
    fem = random.random() < 0.5
    nombre = random.choice(NOMBRES_F if fem else NOMBRES_M)
    apellido = (cliente["apellido"].split()[0] if familia else random.choice(APELLIDOS)) + " " + random.choice(APELLIDOS)
    while True:
        doc = (str(random.randint(1100000000, 1119999999)) if edad(nac, fecha_salida) < 18
               else str(random.randint(1000000000, 1099999999)))
        if doc not in documentos_usados:
            documentos_usados.add(doc)
            break
    return nombre + " " + apellido, doc, nac


def peso_salida(s):
    t = TOUR[s["tour"]]
    if t[12] == "SOLO_FUTURAS":
        return 0
    w = t[9] * temporada(s["fecha"])
    if s["fecha"] >= HOY.date():
        dias = (s["fecha"] - HOY.date()).days
        w *= 1.0 if dias <= 20 else (0.7 if dias <= 45 and temporada(s["fecha"]) > 2 else 0.25)
    return w


pesos = [peso_salida(s) for s in SALIDAS]
pesos_clientes = [c["peso"] for c in CLIENTES]
intentos = 0
while len(RESERVAS) < 520 and intentos < 20000:
    intentos += 1
    s = random.choices(SALIDAS, pesos)[0]
    t = TOUR[s["tour"]]
    disponibles = s["cupo"] - ocupados.get(s["id"], 0)
    if disponibles < 1:
        continue
    c = random.choices(CLIENTES, pesos_clientes)[0]
    if c["id"] in clientes_salida.get(s["id"], set()) or edad(c["nac"], s["fecha"]) < t[6]:
        continue
    inicio = datetime.combine(s["fecha"], s["hora"])
    # canal y fecha de creación
    if c["id"] in PERFILES:
        canal = random.choices(["WEB", "APP_MOVIL", "TELEFONO", "AGENCIA_INTERMEDIARIA"], [55, 30, 7, 8])[0]
    else:
        canal = random.choices(["MOSTRADOR", "TELEFONO", "AGENCIA_INTERMEDIARIA"], [60, 30, 10])[0]
    anticipo_dias = random.choices([random.randint(0, 3), random.randint(4, 10), random.randint(11, 30),
                                    random.randint(31, 60)], [15, 30, 40, 15])[0]
    creada = datetime.combine(s["fecha"] - timedelta(days=anticipo_dias), time(random.randint(7, 21), random.randint(0, 59)))
    creada = max(creada, datetime(2025, 1, 2, random.randint(8, 20), random.randint(0, 59)))
    if creada >= HOY:
        creada = HOY - timedelta(days=random.randint(1, 25), minutes=random.randint(0, 900))
    if creada > inicio - timedelta(hours=1):
        creada = inicio - timedelta(hours=random.randint(2, 6))
    if creada.date() > s["fecha"] or creada < datetime(2025, 1, 1):
        continue
    if s["estado"] == "CANCELADA" and creada >= s["cancelada_en"] - timedelta(hours=2):
        continue
    # plan vigente y activo en la fecha del servicio
    planes = [pl for pl in PLANES_POR_PRESTADOR.get(t[1], [])
              if pl[6] <= s["fecha"] <= pl[7] and (pl[8] == "ACTIVO" or creada.date() < INACTIVADO_EN[pl[0]])]
    if not planes:
        continue
    pl = random.choices(planes, [PESO_PLAN[p[4]] for p in planes])[0]
    # participantes
    n = min(random.choices([1, 2, 3, 4, 5, 6], [33, 36, 12, 12, 4, 3])[0], disponibles)
    rid = len(RESERVAS) + 1
    gente = [(c["nombre"] + " " + c["apellido"], c["doc"], c["nac"])]
    for _ in range(n - 1):
        gente.append(acompanante(c, s["fecha"], t[6], t[6] < 18))
    if any(g[1] in inscritos.get(s["id"], set()) for g in gente):
        continue
    # estado de la reserva
    if s["estado"] == "CANCELADA":
        estado, cancelada, por_operador = "CANCELADA", s["cancelada_en"], True
    elif s["estado"] == "REALIZADA":
        if random.random() < 0.09:
            estado, por_operador = "CANCELADA", False
            cancelada = creada + (inicio - creada) * random.uniform(0.15, 0.95)
        else:
            estado, cancelada, por_operador = "COMPLETADA", None, False
    else:
        r = random.random()
        if r < 0.12:
            estado, por_operador = "CANCELADA", False
            cancelada = creada + (HOY - timedelta(hours=3) - creada) * random.uniform(0.2, 0.95)
        else:
            estado, cancelada, por_operador = ("CONFIRMADA" if r < 0.60 else "PENDIENTE"), None, False
    if cancelada is not None and cancelada <= creada:
        cancelada = creada + timedelta(hours=1)
    valor = Decimal(t[7]) * n
    reserva = dict(id=rid, creada=creada, servicio=s["fecha"], estado=estado, valor=valor, cancelada=cancelada,
                   canal=canal, cliente=c["id"], plan=pl[0], salida=s["id"], por_operador=por_operador, reembolso=None)
    RESERVAS.append(reserva)
    clientes_salida.setdefault(s["id"], set()).add(c["id"])
    for num, (nom, doc, nac) in enumerate(gente, 1):
        PARTICIPANTES.append((rid, num, nom, doc, nac))
    if estado != "CANCELADA":
        ocupados[s["id"]] = ocupados.get(s["id"], 0) + n
        inscritos.setdefault(s["id"], set()).update(g[1] for g in gente)

    # ------------------------------------------------------------------ pagos
    def con_intento_fallido(fecha):
        if canal in ("WEB", "APP_MOVIL") and random.random() < 0.12:
            pagar(reserva, valor if random.random() < 0.6 else valor / 2, fecha - timedelta(minutes=random.randint(2, 9)),
                  "FALLIDO", "TARJETA_CREDITO")

    primer = creada + timedelta(minutes=random.randint(5, 90))
    if estado == "COMPLETADA" or estado == "CONFIRMADA":
        con_intento_fallido(primer)
        if random.random() < (0.62 if estado == "COMPLETADA" else 0.55):
            pagar(reserva, valor, primer, "EXITOSO")
        else:
            pagar(reserva, valor / 2, primer, "EXITOSO")
            if estado == "COMPLETADA":
                saldo_en = max(primer + timedelta(hours=1),
                               inicio - timedelta(hours=random.choice([1, 2, 26, 50])))
                pagar(reserva, valor / 2, saldo_en, "EXITOSO", random.choice(["EFECTIVO", "TARJETA_DEBITO", "TRANSFERENCIA"]))
    elif estado == "PENDIENTE":
        r = random.random()
        if r < 0.35:
            pagar(reserva, valor, primer, "PENDIENTE", "PSE")
        elif r < 0.55:
            pagar(reserva, valor, primer, "FALLIDO", "TARJETA_CREDITO")
    else:  # CANCELADA
        if random.random() < (0.85 if por_operador else 0.75):
            pagado_en = min(primer, cancelada - timedelta(minutes=30))
            if pagado_en <= creada:
                pagado_en = creada + timedelta(minutes=5)
            pagado = valor if random.random() < 0.5 else valor / 2
            pagar(reserva, pagado, pagado_en, "EXITOSO")
            if por_operador:
                devolver = pagado
            elif (s["fecha"] - cancelada.date()).days > 5:
                devolver = (pagado * Decimal("0.80")).quantize(Decimal("1"))
            else:
                devolver = None
            if devolver:
                p = pagar(reserva, devolver, cancelada + timedelta(minutes=random.randint(10, 60)), "REEMBOLSADO",
                          PAGOS[-1]["metodo"])
                reserva["reembolso"] = p

# ============================================================================ notificaciones
NOTIFICACIONES = []


def pesos_col(v):
    return f"$ {int(v):,}".replace(",", ".")


def notificar(cliente_id, reserva_id, tipo, mensaje, programada):
    perfil = PERFILES.get(cliente_id)
    canal = perfil["canal"] if perfil else "SMS"
    if programada <= HOY:
        estado = random.choices(["ENVIADA", "LEIDA", "FALLIDA"], [58, 35, 7])[0]
        envio = None if estado == "FALLIDA" else programada + timedelta(minutes=random.randint(0, 15))
    else:
        estado, envio = "PENDIENTE", None
    NOTIFICACIONES.append(dict(id=len(NOTIFICACIONES) + 1, tipo=tipo, canal=canal, mensaje=mensaje,
                               programada=programada, envio=envio, estado=estado, cliente=cliente_id,
                               reserva=reserva_id))


def en_ingles(cliente_id):
    return PERFILES.get(cliente_id, {}).get("idioma") == "INGLES"


eventos = []
for r in RESERVAS:
    s, t = SALIDA[r["salida"]], TOUR[SALIDA[r["salida"]]["tour"]]
    f, h = s["fecha"].strftime("%d/%m/%Y"), s["hora"].strftime("%H:%M")
    en = en_ingles(r["cliente"])
    eventos.append((r["creada"] + timedelta(minutes=1), r["cliente"], r["id"], "CONFIRMACION_RESERVA",
                    f"Booking #{r['id']} for {t[2]} on {f} at {h} has been registered." if en else
                    f"Tu reserva #{r['id']} para {t[2]} el {f} a las {h} quedó registrada."))
    for p in [p for p in PAGOS if p["reserva"] == r["id"]]:
        if p["estado"] == "EXITOSO":
            eventos.append((p["fecha"] + timedelta(minutes=1), r["cliente"], r["id"], "CONFIRMACION_PAGO",
                            f"We received your payment of {pesos_col(p['monto'])} for booking #{r['id']}." if en else
                            f"Recibimos tu pago de {pesos_col(p['monto'])} para la reserva #{r['id']}."))
        elif p["estado"] == "FALLIDO":
            eventos.append((p["fecha"] + timedelta(minutes=1), r["cliente"], r["id"], "CONFIRMACION_PAGO",
                            f"Your payment for booking #{r['id']} could not be processed." if en else
                            f"No pudimos procesar tu pago de {pesos_col(p['monto'])} para la reserva #{r['id']}. Intenta con otro medio."))
    perfil = PERFILES.get(r["cliente"])
    if perfil and r["estado"] != "CANCELADA":
        programada = datetime.combine(s["fecha"] - timedelta(days=perfil["anticipacion"]), time(8, 0))
        if programada >= r["creada"]:
            eventos.append((programada, r["cliente"], r["id"], "RECORDATORIO_RESERVA",
                            f"Reminder: {t[2]} on {f} at {h}. Please arrive 15 minutes early." if en else
                            f"Recuerda: {t[2]} el {f} a las {h}. Llega 15 minutos antes."))
    if r["estado"] == "CANCELADA":
        rb = r["reembolso"]
        if r["por_operador"]:
            msg = (f"La salida de {t[2]} del {f} fue cancelada por el operador."
                   + (f" Te reembolsamos el 100 %: {pesos_col(rb['monto'])}." if rb else ""))
        else:
            msg = (f"Tu reserva #{r['id']} fue cancelada."
                   + (f" Reembolso del 80 % de lo pagado: {pesos_col(rb['monto'])}." if rb else
                      " No aplica reembolso (cancelación con 5 días o menos, o sin pagos)."))
        eventos.append((r["cancelada"] + timedelta(minutes=2), r["cliente"], r["id"], "CAMBIO_ESTADO", msg))

CAMPANAS = [(datetime(2025, 3, 3, 9), "Semana Santa en el Quindío: 10 % de descuento en tours de naturaleza."),
            (datetime(2025, 6, 2, 9), "Vacaciones de mitad de año: planes en familia para el Paisaje Cultural Cafetero."),
            (datetime(2025, 9, 15, 9), "Septiembre cafetero: catas y tours de café con precio especial."),
            (datetime(2025, 11, 20, 9), "Temporada de diciembre: reserva tu noche de velas en Quimbaya."),
            (datetime(2026, 2, 2, 9), "Temporada baja, mejores precios: parapente y canopy entre semana."),
            (datetime(2026, 5, 18, 9), "Reserva tus vacaciones de mitad de año con anticipo del 50 %."),
            (datetime(2026, 9, 7, 9), "Receso de octubre: aventura en Buenavista con cupos limitados."),
            (datetime(2026, 11, 20, 9), "Fin de año en el Quindío: nuevas salidas al Valle de Cocora.")]
aceptan = [cid for cid, p in PERFILES.items() if p["promos"]]
for cuando, texto in CAMPANAS:
    for cid in random.sample(aceptan, int(len(aceptan) * random.uniform(0.55, 0.8))):
        eventos.append((cuando + timedelta(minutes=random.randint(0, 40)), cid, None, "PROMOCION", texto))

eventos.sort(key=lambda e: e[0])
for programada, cid, rid, tipo, msg in eventos:
    notificar(cid, rid, tipo, msg, programada)

# ============================================================================ usuarios internos
USUARIOS = [(1, "admin", "ACTIVO", 1, None), (2, "operador1", "ACTIVO", 3, None), (3, "operador2", "ACTIVO", 3, None),
            (4, "cocora.encargado", "ACTIVO", 2, 1), (5, "aventura.encargado", "ACTIVO", 2, 2),
            (6, "cafe.encargado", "ACTIVO", 2, 3), (7, "parapente.encargado", "ACTIVO", 2, 5),
            (8, "mirador.encargado", "BLOQUEADO", 2, 13), (9, "pijao.encargado", "BLOQUEADO", 2, 10)]

# ============================================================================ escritura del SQL
conteo = {"municipio": len(MUNICIPIOS), "tipo_prestador": len(TIPOS), "rol": len(ROLES),
          "categoria_tour": len(CATEGORIAS), "idioma": len(IDIOMAS), "prestador": len(PRESTADORES),
          "plan": len(PLANES), "usuario_sistema": len(USUARIOS), "cliente": len(CLIENTES),
          "perfil_cliente": len(PERFILES), "tour": len(TOURS), "guia": len(GUIAS),
          "guia_idioma": sum(len(g[4]) for g in GUIAS), "salida_tour": len(SALIDAS), "reserva": len(RESERVAS),
          "participante": len(PARTICIPANTES), "pago": len(PAGOS), "notificacion": len(NOTIFICACIONES)}

secciones = []


def seccion(titulo, explicacion, sql):
    secciones.append(f"-- {'-' * 69}\n-- {titulo}\n" + "".join(f"--   {l}\n" for l in explicacion) +
                     f"-- {'-' * 69}\n{sql}\n")


seccion("MUNICIPIO (12): los 12 municipios del Quindío con su código DANE", [],
        insert("municipio", ["id_municipio", "nombre", "codigo_dane"], MUNICIPIOS))
seccion("TIPO_PRESTADOR (8)", ["Catálogo; se pueden agregar tipos sin cambiar el modelo."],
        insert("tipo_prestador", ["id_tipo_prestador", "nombre_tipo"], TIPOS))
seccion("ROL (3)", [], insert("rol", ["id_rol", "nombre"], ROLES))
seccion("CATEGORIA_TOUR (4) e IDIOMA (5)", [],
        insert("categoria_tour", ["id_categoria", "nombre"], sorted(CATEGORIAS.items())) + "\n" +
        insert("idioma", ["id_idioma", "nombre_idioma"], sorted(IDIOMAS.items())))
seccion("PRESTADOR (20)", [
    "Repartidos de forma desigual: Salento concentra la oferta y Córdoba no tiene prestadores.",
    "12 operan tours; los demás (hoteles, transporte, restaurantes, alquiler) solo publican planes.",
    "Rafting Río Barragán está SUSPENDIDO (sin salidas desde junio de 2026) y Senderos de Pijao",
    "está RETIRADO (solo historial de 2025): ninguno se borra y su historial se conserva."],
        insert("prestador", ["id_prestador", "nombre_comercial", "nit_documento", "numero_rnt", "direccion",
                             "telefono", "correo", "estado", "id_municipio", "id_tipo_prestador"],
               [(p[0], p[1], p[2], p[3], p[4], telefono(), p[8], p[7], p[5], p[6]) for p in PRESTADORES]))
seccion("PLAN (40)", [
    "Cada operador tiene planes grupales por temporada; algunos tienen privado o todo incluido.",
    "Planes vencidos: 'Grupal temporada 2025' (fecha_fin 2025-12-31) siguen ACTIVO pero ya no",
    "están vigentes. Planes INACTIVO: los del prestador retirado y suspendido, y un menú vencido.",
    "Un plan inactivo conserva las reservas que se vendieron mientras estuvo activo."],
        insert("plan", ["id_plan", "id_prestador", "nombre", "descripcion", "tipo", "precio", "fecha_inicio",
                        "fecha_fin", "estado"], [(p[0], p[1], p[2], p[3], p[4], Decimal(p[5]), p[6], p[7], p[8]) for p in PLANES]))
seccion("USUARIO_SISTEMA (9)", [
    "Contraseña de prueba de TODOS los usuarios: Clave123*  (guardada cifrada con BCrypt).",
    "admin = administrador; operador1 y operador2 = operadores; *.encargado = encargados de prestador.",
    "mirador.encargado y pijao.encargado están BLOQUEADO (no pueden iniciar sesión)."],
        insert("usuario_sistema", ["id_usuario", "nombre_usuario", "contrasena_cifrada", "estado", "id_rol",
                                   "id_prestador"], [(u[0], u[1], hash_clave(), u[2], u[3], u[4]) for u in USUARIOS]))
con_perfil = len(PERFILES)
seccion(f"CLIENTE (150) y PERFIL_CLIENTE ({con_perfil})", [
    f"{con_perfil} clientes tienen cuenta y perfil; los demás se registraron en el mostrador.",
    f"{len(EXTRANJEROS)} extranjeros (pasaporte o cédula de extranjería), ciudades de origen variadas.",
    "Cuenta de prueba de cliente: valentina.rojas@gmail.com / Clave123*  (todas usan Clave123*).",
    "El correo de la cuenta es el mismo correo de contacto del cliente."],
        insert("cliente", ["id_cliente", "nombre", "apellido", "tipo_documento", "numero_documento", "correo",
                           "telefono", "fecha_nacimiento", "ciudad_origen"],
               [(c["id"], c["nombre"], c["apellido"], c["tipo"], c["doc"], c["correo"], c["tel"], c["nac"], c["ciudad"])
                for c in CLIENTES]) + "\n" +
        insert("perfil_cliente", ["id_cliente", "correo_cuenta", "contrasena_cifrada", "idioma_preferencia",
                                  "canal_notificacion_preferido", "anticipacion_recordatorios", "acepta_promociones"],
               [(cid, p["correo"], hash_clave(), p["idioma"], p["canal"], p["anticipacion"],
                 p["promos"]) for cid, p in sorted(PERFILES.items())]))
seccion(f"TOUR (22), GUIA (14) y GUIA_IDIOMA ({conteo['guia_idioma']})", [
    "'Taller de cestería en Filandia' no tiene salidas y 'Tour arqueológico quimbaya' tiene",
    "salidas sin participantes: así el reporte 9 muestra los dos casos.",
    "Todos los guías hablan español; algunos también inglés, francés, alemán o portugués."],
        insert("tour", ["id_tour", "nombre", "descripcion", "duracion_horas", "dificultad", "edad_minima",
                        "precio_persona", "id_categoria", "id_prestador"],
               [(t[0], t[2], t[3], Decimal(t[4]), t[5], t[6], Decimal(t[7]), t[8], t[1]) for t in TOURS]) + "\n" +
        insert("guia", ["id_guia", "nombre", "documento", "tarjeta_profesional"], [g[:4] for g in GUIAS]) + "\n" +
        insert("guia_idioma", ["id_guia", "id_idioma"], [(g[0], i) for g in GUIAS for i in g[4]]))
seccion(f"SALIDA_TOUR ({len(SALIDAS)})", [
    "Enero de 2025 a diciembre de 2026, con más salidas en temporada alta (diciembre-enero,",
    "Semana Santa, mitad de año y receso de octubre) y menos en febrero y septiembre.",
    "Cada salida usa un idioma que su guía habla, y un guía hace como máximo una salida por día,",
    "así nunca se cruzan sus horarios. Salidas pasadas: REALIZADA o CANCELADA; futuras: PROGRAMADA.",
    "La salida de Cocora del 2026-11-18 o posterior marcada CANCELADA la canceló el operador."],
        insert("salida_tour", ["id_salida", "fecha_salida", "hora_salida", "cupo", "estado", "id_tour", "id_guia",
                               "id_idioma"],
               [(s["id"], s["fecha"], s["hora"], s["cupo"], s["estado"], s["tour"], s["guia"], s["idioma"]) for s in SALIDAS]))
seccion(f"RESERVA ({len(RESERVAS)}) y PARTICIPANTE ({len(PARTICIPANTES)})", [
    "fecha_servicio = fecha de la salida; valor_total = precio por persona del tour x participantes.",
    "El plan es del mismo prestador que el tour y estaba vigente y activo cuando se reservó.",
    "Nunca se supera el cupo, todos cumplen la edad mínima y nadie se repite en la misma salida.",
    "Salidas realizadas: COMPLETADA (pagadas) o CANCELADA por el cliente. Salidas canceladas por el",
    "operador: todas sus reservas CANCELADA con reembolso del 100 %. Salidas futuras: PENDIENTE",
    "(sin pago exitoso), CONFIRMADA (con anticipo o pago total) o CANCELADA.",
    "El primer participante de cada reserva es el propio cliente."],
        insert("reserva", ["id_reserva", "fecha_creacion", "fecha_servicio", "estado", "valor_total",
                           "fecha_cancelacion", "canal", "id_cliente", "id_plan", "id_salida"],
               [(r["id"], r["creada"], r["servicio"], r["estado"], r["valor"], r["cancelada"], r["canal"], r["cliente"],
                 r["plan"], r["salida"]) for r in RESERVAS]) + "\n" +
        insert("participante", ["id_reserva", "num_participante", "nombre", "documento", "fecha_nacimiento"],
               PARTICIPANTES, por_bloque=100))
seccion(f"PAGO ({len(PAGOS)})", [
    "Pago único o anticipo del 50 % + saldo; algunos intentos FALLIDO antes del pago exitoso y",
    "pagos PENDIENTE en reservas sin confirmar. Tarjeta y PSE llevan referencia única.",
    "Reembolsos (estado REEMBOLSADO): 80 % si el cliente canceló con más de 5 días de",
    "anticipación, 100 % si el operador canceló la salida; con 5 días o menos no hay reembolso.",
    "Los pagos exitosos de una reserva nunca superan su valor total."],
        insert("pago", ["id_pago", "fecha", "monto", "metodo", "estado", "referencia_transaccion", "id_reserva"],
               [(p["id"], p["fecha"], p["monto"].quantize(Decimal("1")), p["metodo"], p["estado"], p["ref"], p["reserva"])
                for p in PAGOS], por_bloque=100))
seccion(f"NOTIFICACION ({len(NOTIFICACIONES)})", [
    "Confirmación al crear cada reserva, confirmación de cada pago exitoso o fallido, cambio de",
    "estado al cancelar, recordatorios según el perfil (N días antes a las 8:00, por el canal",
    "preferido y en inglés si el perfil lo pide) y promociones solo a quienes las aceptan (sin reserva).",
    "Las ya programadas están ENVIADA, LEIDA o FALLIDA; las futuras siguen PENDIENTE."],
        insert("notificacion", ["id_notificacion", "tipo", "canal", "mensaje", "fecha_programada", "fecha_envio",
                                "estado", "id_cliente", "id_reserva"],
               [(n["id"], n["tipo"], n["canal"], n["mensaje"], n["programada"], n["envio"], n["estado"], n["cliente"],
                 n["reserva"]) for n in NOTIFICACIONES], por_bloque=100))

resumen = "\n".join(f"--   {t:<16} {n:>5}" for t, n in conteo.items())
encabezado = f"""-- =====================================================================
-- QuindíoStay · Módulo Tours y Actividades
-- Proyecto Final - Bases de Datos I (02D) · Universidad del Quindío · 2026-2
--
-- 02_DML_GrupoNN.sql  ·  Orden de ejecución: 2 de 3 (después de 01_DDL)
-- Datos de ejemplo verosímiles y asimétricos, de enero de 2025 a
-- diciembre de 2026. Fecha de corte: 15 de noviembre de 2026 (las
-- salidas anteriores ya ocurrieron; las posteriores están programadas).
--
-- Registros por tabla:
{resumen}
--
-- Cuentas de prueba (contraseña de todas: Clave123*):
--   Administrador ........ admin
--   Encargado prestador .. cocora.encargado   (Cocora Expediciones SAS)
--   Operador ............. operador1
--   Cliente .............. valentina.rojas@gmail.com
--
-- Los datos cumplen las reglas que valida la aplicación (cupo, idioma del
-- guía, cruces de horario, edad mínima, plan vigente del mismo prestador,
-- reembolsos). El script 04_Verificacion_GrupoNN.sql lo comprueba.
-- =====================================================================

SET NAMES utf8mb4;
USE tours_actividades;
START TRANSACTION;
"""
with open("02_DML_GrupoNN.sql", "w", encoding="utf-8") as f:
    f.write(encabezado + "\n" + "\n".join(secciones) + "\nCOMMIT;\n")
for t, n in conteo.items():
    print(f"{t:<16} {n:>5}")
