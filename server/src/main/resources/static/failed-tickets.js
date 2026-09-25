// ============================================================
// FAILED TICKETS PAGE
// ============================================================

document.addEventListener("DOMContentLoaded", () => {
    loadFailedEmbeddings();
});


// ============================================================
// LOAD FAILED EMBEDDINGS
// ============================================================

async function loadFailedEmbeddings() {

    const container = document.getElementById("failedEmbeddingsList");
    const countElement = document.getElementById("failedEmbeddingCount");
    const emptyState = document.getElementById("failedEmptyState");

    try {

        console.log("Loading failed embeddings...");

        const response = await fetch("/tickets/embedding/failed", {
            method: "GET",
            headers: {
                "Accept": "application/json"
            }
        });

        if (!response.ok) {
            throw new Error(
                `Failed embeddings API returned HTTP ${response.status}`
            );
        }

        const failedTickets = await response.json();

        console.log(
            "Failed embedding tickets:",
            failedTickets
        );

        if (!Array.isArray(failedTickets)) {
            throw new Error("Invalid failed-ticket response.");
        }

        if (countElement) {
            countElement.textContent =
                `${failedTickets.length} ticket${failedTickets.length === 1 ? "" : "s"}`;
        }

        if (failedTickets.length === 0) {

            if (container) {
                container.innerHTML = "";
            }

            if (emptyState) {
                emptyState.classList.remove("hidden");
            }

            return;
        }

        if (emptyState) {
            emptyState.classList.add("hidden");
        }

        renderFailedEmbeddings(
            failedTickets,
            container
        );

    } catch (error) {

        console.error(
            "Unable to load failed embeddings:",
            error
        );

        if (container) {
            container.innerHTML = `
                <div class="no-failed-tickets">
                    Unable to load failed tickets.
                </div>
            `;
        }
    }
}


// ============================================================
// RENDER FAILED EMBEDDINGS
// ============================================================

function renderFailedEmbeddings(
    tickets,
    container
) {

    if (!container) {
        return;
    }

    container.innerHTML = "";

    tickets.forEach(ticket => {

        const card =
            createFailedEmbeddingCard(ticket);

        container.appendChild(card);

    });
}


// ============================================================
// CREATE FAILED TICKET CARD
// ============================================================

function createFailedEmbeddingCard(ticket) {

    const card =
        document.createElement("div");

    card.className =
        "failed-embedding-card";


    const ticketId =
        ticket.ticketId || "Unknown Ticket";


    const title =
        ticket.title &&
        String(ticket.title).trim()
            ? ticket.title
            : "Untitled ticket";


    const attempts =
        ticket.embeddingAttempts ?? 0;


    card.innerHTML = `

        <div class="failed-embedding-info">

            <button
                    class="failed-embedding-id"
                    type="button"
            >
                ${escapeHtml(ticketId)}
            </button>

            <div class="failed-embedding-title">
                ${escapeHtml(title)}
            </div>

        </div>


        <div class="failed-embedding-meta">

            <span class="failed-embedding-attempts">
                ${attempts} attempts
            </span>

            <span class="failed-embedding-status">
                FAILED
            </span>

            <button
                    class="retry-button"
                    type="button"
            >
                Retry
            </button>

        </div>
    `;


    // ----------------------------------------
    // Open ticket details
    // ----------------------------------------

    const ticketButton =
        card.querySelector(
            ".failed-embedding-id"
        );

    if (ticketButton) {

        ticketButton.addEventListener(
            "click",
            () => openTicket(ticketId)
        );

    }


    // ----------------------------------------
    // Retry embedding
    // ----------------------------------------

    const retryButton =
        card.querySelector(
            ".retry-button"
        );

    if (retryButton) {

        retryButton.addEventListener(
            "click",
            event => {

                event.stopPropagation();

                retryEmbedding(
                    ticketId,
                    retryButton
                );

            }
        );

    }


    return card;
}


// ============================================================
// RETRY EMBEDDING
// ============================================================

async function retryEmbedding(
    ticketId,
    button
) {

    if (!ticketId) {
        return;
    }


    if (button) {

        button.disabled = true;
        button.textContent = "Retrying...";

    }


    try {

        console.log(
            "Retrying embedding:",
            ticketId
        );


        const response = await fetch(
            `/tickets/${encodeURIComponent(ticketId)}/embedding/retry`,
            {
                method: "POST",
                headers: {
                    "Accept": "text/plain"
                }
            }
        );


        if (!response.ok) {

            const errorText =
                await response.text();

            throw new Error(
                errorText ||
                `HTTP ${response.status}`
            );

        }


        const message =
            await response.text();


        console.log(
            "Retry response:",
            message
        );


        // Reload the page data.
        await loadFailedEmbeddings();

    } catch (error) {

        console.error(
            "Embedding retry failed:",
            error
        );


        alert(
            "Unable to retry embedding.\n\n" +
            "Ticket: " +
            ticketId +
            "\n\n" +
            "Error: " +
            error.message
        );


        if (button) {

            button.disabled = false;
            button.textContent = "Retry";

        }

    }
}


// ============================================================
// OPEN TICKET DETAILS
// ============================================================

async function openTicket(ticketId) {

    try {

        console.log(
            "Opening ticket:",
            ticketId
        );


        const response =
            await fetch(
                `/tickets/${encodeURIComponent(ticketId)}`
            );


        if (!response.ok) {

            throw new Error(
                `Ticket API returned HTTP ${response.status}`
            );

        }


        const ticket =
            await response.json();


        renderTicketDetails(ticket);


    } catch (error) {

        console.error(
            "Unable to open ticket:",
            error
        );


        alert(
            "Unable to load ticket details.\n\n" +
            error.message
        );

    }
}


// ============================================================
// RENDER TICKET DETAILS
// ============================================================

function renderTicketDetails(ticket) {

    const modal =
        document.getElementById("ticketModal");

    const modalId =
        document.getElementById("modalTicketId");

    const modalBody =
        document.getElementById("modalBody");


    if (!modal || !modalId || !modalBody) {
        return;
    }


    modalId.textContent =
        ticket.ticketId || "Ticket";


    modalBody.innerHTML = `

        <div class="ticket-detail-grid">

            <div>
                <strong>Created</strong>
                <span>
                    ${escapeHtml(ticket.created || "-")}
                </span>
            </div>

            <div>
                <strong>State</strong>
                <span>
                    ${escapeHtml(ticket.state || "-")}
                </span>
            </div>

            <div>
                <strong>Category</strong>
                <span>
                    ${escapeHtml(ticket.category || "-")}
                </span>
            </div>

            <div>
                <strong>Urgency</strong>
                <span>
                    ${ticket.urgency ?? "-"}
                </span>
            </div>

            <div>
                <strong>Customer</strong>
                <span>
                    ${escapeHtml(ticket.customerName || "-")}
                </span>
            </div>

            <div>
                <strong>Phone</strong>
                <span>
                    ${escapeHtml(ticket.customerPhone || "-")}
                </span>
            </div>

        </div>


        <div class="ticket-detail-section">

            <strong>Title</strong>

            <p>
                ${escapeHtml(ticket.title || "-")}
            </p>

        </div>


        <div class="ticket-detail-section">

            <strong>Description</strong>

            <p>
                ${escapeHtml(ticket.description || "-")}
            </p>

        </div>


        <div class="ticket-detail-section">

            <strong>Resolution</strong>

            <p>
                ${escapeHtml(ticket.resolution || "-")}
            </p>

        </div>


        <div class="ticket-detail-section">

            <strong>Conversation</strong>

            <p>
                ${escapeHtml(ticket.conversation || "-")}
            </p>

        </div>

    `;


    modal.classList.remove("hidden");
}


// ============================================================
// CLOSE TICKET
// ============================================================

function closeTicket() {

    const modal =
        document.getElementById("ticketModal");

    if (modal) {
        modal.classList.add("hidden");
    }

}


// ============================================================
// ESCAPE HTML
// ============================================================

function escapeHtml(value) {

    return String(value)
        .replace(/&/g, "&amp;")
        .replace(/</g, "&lt;")
        .replace(/>/g, "&gt;")
        .replace(/"/g, "&quot;")
        .replace(/'/g, "&#039;");

}