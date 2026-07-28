# IraqueCoreV2

**IraqueCoreV2** nace de la idea de tener todo en un solo lugar. Antes existían **IraqueCore** e **IraqueScoreboard** como dos plugins separados, cada uno haciendo lo suyo. Esta versión los fusiona en uno solo, los mejora, y encima les agrega montones de cosas nuevas para que el server tenga esa experiencia completita pero sin sentirse overkill.

Este plugin está hecho para servidores **semi-vanilla** donde lo importante es jugar tranqui con amigos, no tener mil comandos raros ni sistemas complicados. Todo es configurable, todo se puede prender o apagar, y todo se ve bonito.

---

## Qué trae

### Cosas de jugador

| Comando | Para qué sirve |
|---------|---------------|
| `/spawn` | Volvé al spawn |
| `/msg` y `/r` | Hablale en privado a alguien |
| `/tags` | Abrí el menú de tags para poner un prefijo copado en tu chat |
| `/scoreboard` | Prendé o apagá el scoreboard |
| `/playtime` | Cuánto tiempo llevás jugado |
| `/stats` | Tus estadísticas: bloques rotos, muertes, tiempo, rango... |
| `/leaderboards` | Tablas de líderes: bloques, muertes, tiempo |
| `/gm` | Cambiá tu modo de juego |
| `/iraquecore` | Información del plugin |
| `/whitelist` | Administración de la whitelist |

### Cosas automáticas

- **Rangos** — con prefijos, sufijos y colores. Definís los tuyos en config.
- **Chat formateado** — mostrá rango, tag y colores en cada mensaje.
- **AFK** — después de unos minutos sin moverte, te marca como ausente.
- **Dormir** — si suficiente gente duerme, se salta la noche al toque.
- **Tumbas** — cuando morís, tus cosas quedan en un cofre en el lugar.
- **Advertencia de durabilidad** — te avisa cuando tu herramienta está por romperse.
- **Colores en yunques** — usá `&` y `&#RRGGBB` en los renombres.
- **Editor de armor stands** — agachate + click derecho y editalos desde un menú.
- **MOTD animado** — el mensaje del servidor puede tener frames que cambian solos.
- **Scoreboard animado** — título con onda y estadísticas en vivo.

### Cosas técnicas

- **Integración con Discord** — chat puente, avisos de logros, muertes, joins/leaves, y hasta whitelist automática desde Discord.
- **Colores hex** — usá `&#RRGGBB` en cualquier lado (messages.yml, tags, scoreboard, items).
- **Sistema de almacenamiento** — YAML por defecto, con soporte para SQLite y MySQL.
- **Placeholders** — `{player}`, `{prefix}`, `{tag}`, `{online}`, `{world}` y muchos más.
- **Multilenguaje** — todos los mensajes están en `messages.yml`, traducilos como quieras.

---

## Cómo se usa

1. Tiramos el `.jar` en `plugins/`
2. Reiniciamos el server
3. Configuramos `config.yml`, `messages.yml`, `discord.yml`, `tags.yml` y `motd.yml` a gusto
4. `/reload` o `/icreload` para aplicar los cambios

### Permisos principales

| Permiso | Default | Qué hace |
|---------|---------|----------|
| `iraquecore.tags.use` | ✅ | Usar tags |
| `iraquecore.scoreboard` | ✅ | Prender/apagar scoreboard |
| `iraquecore.msg` | ✅ | Mensajes privados |
| `iraquecore.playtime` | ✅ | Ver tu tiempo |
| `iraquecore.stats` | ✅ | Ver stats |
| `iraquecore.anvilcolors` | ✅ | Colores en yunques |
| `iraquecore.armorstand` | ✅ | Editor de armor stands |
| `iraquecore.*` | 🔒 | Todo (solo admins) |

✅ = todos lo tienen · 🔒 = solo admins

---

## Para qué servidores está pensado

Servidores **chill** de **supervivencia semi-vanilla** entre amigos. Sin TPA, sin homes, sin warps. Con lo justo y necesario para que la experiencia sea más cómoda sin dejar de ser Minecraft vainilla.

---

## Créditos

Hecho por **Proctocol** — con mucho amor, mates de por medio, y código que funciona (cuando no hay merge conflicts).

---

> Si llegaste hasta acá, gracias por leer ❤️
