export default function Info({ titulo, children }) {
  return (
    <main className="contenedor py-16 min-h-[55vh]">
      <h1 className="titulo-pagina">{titulo}</h1>
      <div className="texto-apoyo max-w-2xl space-y-3">{children}</div>
    </main>
  )
}
