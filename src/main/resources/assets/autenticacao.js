"use strict";
document.querySelectorAll("[data-senha]").forEach((botao) => {
    const campo = document.getElementById(botao.dataset.senha);
    botao.hidden = false;
    botao.addEventListener("click", () => {
        const visivel = campo.type === "password";
        campo.type = visivel ? "text" : "password";
        botao.textContent = visivel ? botao.dataset.ocultar : botao.dataset.mostrar;
        botao.setAttribute("aria-pressed", String(visivel));
    });
});
