# Reproductor

## Lista de reproducción personalizadas

Existen dos maneras de agregar un álbum a la lista de reproducción. Una cliqueando sobre el elemento en la lista, lo cual hace que la lista de reproducción actual sea el álbum seleccionado. Otra forma es agregarlo al final de la lista, conservando la lista actual en reproducción. Para hacer esto se debe cliquear en el signo `+` a la derecha del álbum deseado.

Para eliminar una canción de la lista de reproducción se debe cliquear en el signo `-` al lado de la canción deseada.

Una vez que se tenga la lista deseada, ésta se puede guardar. Para ésto se debe seleccionar la opción “guardar” en el menú superior y darle el nombre deseado a la lista. Para cargar una lista guardada anteriormente se debe ir a la vista de la colección musical y seleccionar la opción de “listas guardadas” en el menú superior. Allí aparecerán todas las listas guardadas anteriormente, con la opción de eliminarlas si se desea.

Las letras se guardan con el en nombre “\<artista\> - \<track\>” en el almacenamiento interno de la aplicación.

## Letras

Para obtener la letra se analizaron distintas opciones, con muy poco éxito:

  - API de Last.fm: Esta API no permite obtener las letras.
  - API de Lololyrics: Esta API es muy fácil de usar, no requiere registro, es gratis, pero no tiene un gran catálogo de letras.
  - API de MusicMatch: Una API muy buena, muy completa, pero en su versión gratuita solo permite obtener el 30% de la letra.
  - LyricsWikia: Al final se optó por utilizar esta wiki, haciendo el pedido HTML y parseando el resultado hasta obtener la letra.
  
Se posee una caché local, la cual es consultada antes de hacer cualquier consulta externa. Si la canción no se encuentra cacheada y se encuentra en el servidor consultado, ésta es cacheada.

https://www.websequencediagrams.com/
```
title Secuencia de petición de letra

UI->+LyricsWikia: getLyrics(artist, track)
LyricsWikia->+localProv: getLyrics(artist, track)
localProv->+cache: exist()
cache-->-localProv: exist

alt exist = true
    localProv->+cache: getLyrics(artist, track)
    cache-->-localProv: lyrics
    localProv->+context: setLyrics(lyrics)
    context-->-localProv: void
else else
    localProv-->-LyricsWikia: false
    LyricsWikia->+LyricsWikiaServer: getPage(url)
    LyricsWikiaServer-->-LyricsWikia: page
    LyricsWikia->+LyricsWikia: parse(url)
    LyricsWikia-->-LyricsWikia: lyrics
    LyricsWikia->+context: setLyrics(lyrics)
    context-->-LyricsWikia: void
end
```

## Notificación

La notificación fue actualizada y ahora muestra la carátula del álbum, además de poseer botónes para controlar la reproducción. La interacción con el servicio se maneja a través de `PendingIntent`s enviados por medio de una difusión (broadcast). El servicio posee un `BroadcastReceiver` con el cual puede leer esta difusión y controlar el flujo de reproducción.

Para la actualización de la información de la notificación (artista, canción, álbum, carátula), ésta implementa la interfaz `ServiceCallback` definido en el servicio.

## Widget

El widget es muy similar a la notificación. Se comunica con la aplicación por medio de broadcast. Implementa los mismos botones que la notificación, y además agrega un botón de stop para parar el servicio. La misma muestra información sobre el artista, el nombre de la canción en reproducción y la carátula. Como el widget no necesita actualizarse cada cierto periodo, sino que lo hace por medio de eventos, la propiedas `updatePeriodMillis` del archivo `widget_info.xml` fue seteada en cero. Todo el código encargado de actualizar la interfaz del widget se encuentra en `onReceive()`, método heredado de `BroadcastReceiver`.
