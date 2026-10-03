// ============================================================
// CUZOR RAG - script.js
// PostgreSQL + pgvector + HNSW
// ============================================================


// ============================================================
// GLOBAL STATE
// ============================================================

let currentResults = {
    highConfidence: [],
    lowConfidence: []
};

let currentQuery = "";


// ============================================================
// DOM HELPERS
// ============================================================

function getElement(id) {
    return document.getElementById(id);
}


// ============================================================
// INITIALIZATION
// ============================================================

document.addEventListener("DOMContentLoaded", () => {

    createTicketLimitControl();

    updateThresholdLabels();

    loadEmbeddingHealth();

});

// ============================================================
// CREATE "NUMBER OF TICKETS" CONTROL
// ============================================================
//
// This creates the ticket-limit slider from JavaScript.
//
// Therefore you do NOT need to change your HTML.
//
// Default = 20
// Minimum = 1
// Maximum = 40
//
// The selected value is sent to:
//
// POST /tickets/search
//
// {
//     query: "...",
//     limit: selectedValue
// }
// ============================================================

function createTicketLimitControl() {

    const controls =
        getElement("controls");

    if (!controls) {
        return;
    }


    // ========================================================
    // FIND ALL EXISTING "NUMBER OF TICKETS" CARDS
    // ========================================================

    const existingCards =
        Array.from(
            controls.querySelectorAll(".control-card")
        ).filter(card =>
            card.textContent
                .toLowerCase()
                .includes("number of tickets")
        );


    // ========================================================
    // IF A NUMBER-OF-TICKETS CONTROL ALREADY EXISTS
    // ========================================================
    //
    // Use the existing one.
    //
    // Do NOT create another one.
    //
    // If there are accidentally multiple ones,
    // keep the first and remove the rest.
    // ========================================================

    if (existingCards.length > 0) {

        const firstCard =
            existingCards[0];


        // Remove duplicate cards
        for (
            let i = 1;
            i < existingCards.length;
            i++
        ) {

            existingCards[i].remove();
        }


        // ----------------------------------------------------
        // Make sure the existing slider has the correct ID
        // ----------------------------------------------------

        const slider =
            firstCard.querySelector(
                'input[type="range"]'
            );


        if (slider) {

            slider.id =
                "ticketLimit";

            slider.min =
                "1";

            slider.max =
                "40";

            slider.step =
                "1";


            // ------------------------------------------------
            // Find/create value display
            // ------------------------------------------------

            let value =
                firstCard.querySelector(
                    ".threshold-value"
                );


            if (!value) {

                value =
                    document.createElement(
                        "span"
                    );

                value.className =
                    "threshold-value";

                slider.parentElement
                    .querySelector(
                        ".control-header"
                    )
                    ?.appendChild(value);
            }


            if (value) {

                value.id =
                    "ticketLimitValue";

                value.textContent =
                    slider.value;
            }


            // ------------------------------------------------
            // Make sure slider updates the number
            // ------------------------------------------------

            slider.oninput =
                updateTicketLimit;
        }


        return;
    }


    // ========================================================
    // CREATE CONTROL ONLY IF NONE EXISTS
    // ========================================================

    const card =
        document.createElement("div");

    card.className =
        "control-card";


    card.innerHTML = `

        <div class="control-header">

            <div>

                <span class="control-title">
                    Number of Tickets
                </span>

                <span
                    id="ticketLimitValue"
                    class="threshold-value"
                >
                    20
                </span>

            </div>

        </div>


        <input
            id="ticketLimit"
            type="range"
            min="1"
            max="40"
            step="1"
            value="20"
        >


        <div class="range-labels">

            <span>1</span>

            <span>40</span>

        </div>

    `;


    const slider =
        card.querySelector(
            "#ticketLimit"
        );


    if (slider) {

        slider.oninput =
            updateTicketLimit;
    }


    controls.insertBefore(
        card,
        controls.firstChild
    );
}


// ============================================================
// UPDATE TICKET LIMIT DISPLAY
// ============================================================

function updateTicketLimit() {

    const slider =
        getElement("ticketLimit");

    const value =
        getElement("ticketLimitValue");


    if (!slider || !value) {
        return;
    }


    value.textContent =
        slider.value;
}


// ============================================================
// SEARCH ENTER KEY
// ============================================================

function handleSearchKey(event) {

    if (event.key === "Enter") {

        event.preventDefault();

        searchTickets();
    }
}


// ============================================================
// MAIN SEARCH FUNCTION
// ============================================================

async function searchTickets() {

    const input =
        getElement("searchInput");

    const searchButton =
        getElement("searchButton");


    if (!input) {
        return;
    }


    const query =
        input.value.trim();


    // --------------------------------------------------------
    // Validate query
    // --------------------------------------------------------

    if (!query) {

        alert(
            "Please enter a customer problem to search."
        );

        input.focus();

        return;
    }


    currentQuery =
        query;


    // --------------------------------------------------------
    // Get requested number of tickets
    // --------------------------------------------------------

    const limitSlider =
        getElement("ticketLimit");


    let limit =
        limitSlider
            ? parseInt(limitSlider.value, 10)
            : 20;


    // Safety
    if (!Number.isFinite(limit)) {
        limit = 20;
    }


    limit =
        Math.max(
            1,
            Math.min(
                40,
                limit
            )
        );


    // --------------------------------------------------------
    // UI - loading
    // --------------------------------------------------------

    setLoading(true);


    if (searchButton) {

        searchButton.disabled =
            true;

        searchButton.textContent =
            "Searching...";
    }


    hide(
        "resultsSection"
    );


    hide(
        "emptyState"
    );


    try {

        console.log(
            "===================================="
        );

        console.log(
            "Sending RAG Search Request"
        );

        console.log(
            "Query:",
            query
        );

        console.log(
            "Limit:",
            limit
        );

        console.log(
            "===================================="
        );


        // ----------------------------------------------------
        // IMPORTANT
        // ----------------------------------------------------
        //
        // This is the request expected by:
        //
        // data class SearchRequest(
        //     val query: String,
        //     val limit: Int = 20
        // )
        //
        // ----------------------------------------------------

        const response =
            await fetch(
                "/tickets/search",
                {
                    method: "POST",

                    headers: {
                        "Content-Type":
                            "application/json",

                        "Accept":
                            "application/json"
                    },

                    body:
                        JSON.stringify({

                            query:
                            query,

                            limit:
                            limit
                        })
                }
            );


        // ----------------------------------------------------
        // Check HTTP response
        // ----------------------------------------------------

        if (!response.ok) {

            let errorMessage =
                `Server returned HTTP ${response.status}`;


            try {

                const errorText =
                    await response.text();

                if (errorText) {

                    console.error(
                        "Server error:",
                        errorText
                    );

                    errorMessage +=
                        `\n\n${errorText}`;
                }

            } catch (error) {

                console.error(
                    "Could not read error response:",
                    error
                );
            }


            throw new Error(
                errorMessage
            );
        }


        // ----------------------------------------------------
        // Convert response to JSON
        // ----------------------------------------------------

        const data =
            await response.json();


        console.log(
            "RAG Search Response:",
            data
        );


        // ----------------------------------------------------
        // Store results
        // ----------------------------------------------------

        currentResults = {

            highConfidence:
                Array.isArray(
                    data.highConfidence
                )
                    ? data.highConfidence
                    : [],

            lowConfidence:
                Array.isArray(
                    data.lowConfidence
                )
                    ? data.lowConfidence
                    : []
        };


        // ----------------------------------------------------
        // Show results
        // ----------------------------------------------------

        renderResults();


    } catch (error) {

        console.error(
            "Search failed:",
            error
        );


        alert(
            "Unable to search tickets.\n\n" +
            "Please check whether the Ktor server is running.\n\n" +
            "Error: " +
            error.message
        );


        show(
            "emptyState"
        );


    } finally {

        setLoading(false);


        if (searchButton) {

            searchButton.disabled =
                false;

            searchButton.textContent =
                "Search";
        }
    }
}


// ============================================================
// RENDER RESULTS
// ============================================================

function renderResults() {

    const highResults =
        currentResults.highConfidence || [];


    const lowResults =
        currentResults.lowConfidence || [];


    const allResults =
        [
            ...highResults,
            ...lowResults
        ];


    // --------------------------------------------------------
    // Show results section
    // --------------------------------------------------------

    show(
        "resultsSection"
    );


    hide(
        "emptyState"
    );


    // --------------------------------------------------------
    // Total count
    // --------------------------------------------------------

    const resultCount =
        getElement("resultCount");


    if (resultCount) {

        resultCount.textContent =
            `${allResults.length} tickets`;
    }


    // --------------------------------------------------------
    // High count
    // --------------------------------------------------------

    const highCount =
        getElement("highCount");


    if (highCount) {

        highCount.textContent =
            highResults.length;
    }


    // --------------------------------------------------------
    // Low count
    // --------------------------------------------------------

    const lowCount =
        getElement("lowCount");


    if (lowCount) {

        lowCount.textContent =
            lowResults.length;
    }


    // --------------------------------------------------------
    // Render high-confidence tickets
    // --------------------------------------------------------

    renderTicketList(
        "highResults",
        highResults
    );


    // --------------------------------------------------------
    // Render low-confidence tickets
    // --------------------------------------------------------

    renderTicketList(
        "lowResults",
        lowResults
    );
}


// ============================================================
// RENDER TICKET LIST
// ============================================================

function renderTicketList(
    containerId,
    tickets
) {

    const container =
        getElement(containerId);


    if (!container) {
        return;
    }


    container.innerHTML =
        "";


    if (!tickets.length) {

        container.innerHTML = `

            <div class="no-results">

                No matching tickets found.

            </div>     

        `;

        return;
    }


    tickets.forEach(
        ticket => {

            const card =
                createTicketCard(
                    ticket
                );


            container.appendChild(
                card
            );
        }
    );
}


// ============================================================
// CREATE TICKET CARD
// ============================================================

function createTicketCard(
    ticket
) {

    const card =
        document.createElement("div");


    card.className =
        "ticket-card";


    const similarity =
        Number(
            ticket.similarity || 0
        );

    const percentage =
        (
            similarity * 100
        ).toFixed(1);


    const category =
        ticket.category ||
        "UNKNOWN";


    const state =
        ticket.state ||
        "UNKNOWN";


    const title =
        ticket.title &&
        ticket.title.trim()
            ? ticket.title
            : "Untitled ticket";


    const description =
        ticket.description &&
        ticket.description.trim()
            ? ticket.description
            : "No description available.";


    const preview =
        cleanPreview(
            description
        );


    card.innerHTML = `

        <div class="ticket-card-header">

            <div>

                <span class="ticket-id">
                    ${escapeHtml(
        ticket.ticketId || ""
    )}
                </span>

                <h5>
                    ${escapeHtml(title)}
                </h5>

            </div>

            <span class="similarity-badge">
                ${percentage}%
            </span>

        </div>


        <div class="ticket-meta">

            <span>
                ${escapeHtml(category)}
            </span>

            <span>
                ${escapeHtml(state)}
            </span>

        </div>


        <p class="ticket-preview">
            ${escapeHtml(preview)}
        </p>

        <div class="ticket-card-footer">

            <span>
                Similarity
            </span>

            <strong>
                ${similarity.toFixed(3)}
            </strong>

        </div>

    `;


    // --------------------------------------------------------
    // Open complete ticket
    // --------------------------------------------------------

    card.addEventListener(
        "click",
        () => {

            openTicket(
                ticket.ticketId
            );
        }
    );


    return card;
}


// ============================================================
// OPEN COMPLETE TICKET
// ============================================================
//
// IMPORTANT:
//
// We do NOT use the shortened ticket returned by the search
// response for the modal.
//
// Instead we call:
//
// GET /tickets/{ticketId}
//
// This lets PostgreSQL return the complete original ticket,
// including the complete conversation.
// ============================================================

async function openTicket(
    ticketId
) {

    if (!ticketId) {
        return;
    }


    const modal =
        getElement("ticketModal");


    const modalTitle =
        getElement("modalTicketId");


    const modalBody =
        getElement("modalBody");


    if (!modal || !modalBody) {
        return;
    }


    if (modalTitle) {

        modalTitle.textContent =
            ticketId;
    }

    modalBody.innerHTML = `

        <div class="modal-loading">

            Loading complete ticket...

        </div>

    `;


    show(
        "ticketModal"
    );


    try {

        const response =
            await fetch(
                `/tickets/${encodeURIComponent(ticketId)}`,
                {
                    method: "GET",

                    headers: {
                        "Accept":
                            "application/json"
                    }
                }
            );


        if (!response.ok) {

            throw new Error(
                `HTTP ${response.status}`
            );
        }


        const ticket =
            await response.json();


        renderTicketDetails(
            ticket
        );


    } catch (error) {

        console.error(
            "Unable to load ticket:",
            error
        );


        modalBody.innerHTML = `

            <div class="modal-error">

                Unable to load ticket details.

                <br><br>

                ${escapeHtml(
            error.message
        )}

            </div>

        `;
    }
}


// ============================================================
// RENDER COMPLETE TICKET DETAILS
// ============================================================

function renderTicketDetails(
    ticket
) {

    const modalTitle =
        getElement("modalTicketId");


    const modalBody =
        getElement("modalBody");


    if (modalTitle) {

        modalTitle.textContent =
            ticket.ticketId || "Ticket";
    }

    if (!modalBody) {
        return;
    }


    modalBody.innerHTML = `

        ${createDetailSection(
        "Created",
        ticket.created
    )}

        ${createDetailSection(
        "Closed At",
        ticket.closedAt
    )}

        ${createDetailSection(
        "State",
        ticket.state
    )}

        ${createDetailSection(
        "Category",
        ticket.category
    )}

        ${createDetailSection(
        "Urgency",
        ticket.urgency
    )}

        ${createDetailSection(
        "Customer Name",
        ticket.customerName
    )}

        ${createDetailSection(
        "Customer Phone",
        ticket.customerPhone
    )}

        ${createDetailSection(
        "Message Count",
        ticket.messageCount
    )}

        ${createDetailSection(
        "Title",
        ticket.title
    )}

        ${createLargeTextSection(
        "Description",
        ticket.description
    )}

        ${createLargeTextSection(
        "Resolution",
        ticket.resolution
    )}

        ${createConversationSection(
        ticket.conversation
    )}

    `;
}


// ============================================================
// DETAIL SECTION
// ============================================================

function createDetailSection(
    label,
    value
) {

    const safeValue =
        value !== null &&
        value !== undefined &&
        String(value).trim()
            ? String(value)
            : "No data recorded for this field.";


    return `

        <div class="detail-section">

            <h4>
                ${escapeHtml(label)}
            </h4>

            <div class="detail-value">

                ${escapeHtml(
        safeValue
    )}

            </div>

        </div>

    `;
}


// ============================================================
// LARGE TEXT SECTION
// ============================================================

function createLargeTextSection(
    label,
    value
) {

    const safeValue =
        value !== null &&
        value !== undefined &&
        String(value).trim()
            ? String(value)
            : "No data recorded for this field.";


    return `

        <div class="detail-section">

            <h4>
                ${escapeHtml(label)}
            </h4>

            <div class="detail-large">

                ${escapeHtml(
        safeValue
    )}

            </div>

        </div>

    `;
}


// ============================================================
// COMPLETE CONVERSATION
// ============================================================
//
// We preserve the FULL conversation.
//
// We do NOT remove BOT messages here.
//
// Why?
//
// This is the ticket-detail view, not embedding.
//
// Your embedding cleaner removes unnecessary BOT text
// before embedding, but the original conversation remains
// untouched in PostgreSQL.
// ============================================================

function createConversationSection(
    conversation
) {

    if (
        !conversation ||
        !String(conversation).trim()
    ) {

        return `

            <div class="detail-section">

                <h4>
                    Complete Raw Conversation
                </h4>

                <div class="detail-large empty">

                    No data recorded for this field.

                </div>

            </div>

        `;
    }


    const formattedConversation =
        formatConversation(
            String(conversation)
        );


    return `

        <div class="detail-section">

            <h4>
                Complete Raw Conversation
            </h4>

            <div class="conversation-box">

                ${formattedConversation}

            </div>

            <div class="conversation-search-action">

                <button
                    type="button"
                    class="search-related-button"
                    onclick="searchRelatedTicketsFromConversation()"
                >
                    🔍 Search Related Tickets
                </button>

            </div>

        </div>

    `;
}


// ============================================================
// FORMAT CONVERSATION
// ============================================================
//
// Handles:
//
// [Jun 01, 2026 01:03 am] CUSTOMER: ...
// [Jun 01, 2026 01:03 am] BOT: ...
// [Jun 01, 2026 01:03 am] AGENT: ...
//
// Every original line is preserved.
// ============================================================

function formatConversation(
    conversation
) {

    const lines =
        conversation
            .replace(/\r\n/g, "\n")
            .replace(/\r/g, "\n")
            .split("\n");

    return lines
        .map(
            line => {

                const trimmed =
                    line.trim();


                if (!trimmed) {

                    return "<br>";
                }


                let className =
                    "conversation-line";


                if (
                    trimmed.includes(
                        "CUSTOMER:"
                    )
                ) {

                    className +=
                        " customer-message";

                } else if (
                    trimmed.includes(
                        "AGENT:"
                    )
                ) {

                    className +=
                        " agent-message";

                } else if (
                    trimmed.includes(
                        "BOT:"
                    )
                ) {

                    className +=
                        " bot-message";
                }


                return `

                    <div
                        class="${className}"
                    >
                        ${escapeHtml(
                    trimmed
                )}
                    </div>

                `;
            }
        )
        .join("");
}


// ============================================================
// CLOSE TICKET MODAL
// ============================================================

function closeTicket() {

    const modal =
        getElement("ticketModal");


    if (modal) {

        hide(
            "ticketModal"
        );
    }
}


// ============================================
// NEW SEARCH
// ============================================

function resetSearch() {

    // Clear previous search data
    currentResults = null;
    currentQuery = "";


    // Clear search box
    const searchInput =
        document.getElementById("searchInput");

    if (searchInput) {

        searchInput.value = "";

    }


    // Hide old search results
    const resultsSection =
        document.getElementById("resultsSection");

    if (resultsSection) {

        resultsSection.classList.add("hidden");

    }


    // Stop loading state
    const loading =
        document.getElementById("loading");

    if (loading) {

        loading.classList.add("hidden");

    }


    // IMPORTANT:
    // DO NOT HIDE THE CONTROLS.
    //
    // The user-selected filters remain visible
    // and keep their previous values.
    const controls =
        document.getElementById("controls");

    if (controls) {

        controls.classList.remove("hidden");

    }


    // Show empty search state
    const emptyState =
        document.getElementById("emptyState");

    if (emptyState) {

        emptyState.classList.remove("hidden");

    }


    // Clear displayed tickets
    const highResults =
        document.getElementById("highResults");

    if (highResults) {

        highResults.innerHTML = "";

    }


    const lowResults =
        document.getElementById("lowResults");

    if (lowResults) {

        lowResults.innerHTML = "";

    }


    // Reset result counters
    const highCount =
        document.getElementById("highCount");

    if (highCount) {

        highCount.textContent = "0";

    }


    const lowCount =
        document.getElementById("lowCount");

    if (lowCount) {

        lowCount.textContent = "0";

    }


    const resultCount =
        document.getElementById("resultCount");

    if (resultCount) {

        resultCount.textContent = "0 tickets";

    }


    // Keep the existing filter values.
    // Only refresh their displayed labels.
    updateThresholdLabels();


    // Put cursor back into search box
    if (searchInput) {

        searchInput.focus();

    }
}


// ============================================================

// ============================================================

function updateResults() {

    updateThresholdLabels();


    if (!currentResults) {
        return;
    }


    const highThreshold =
        parseFloat(
            getElement(
                "highThreshold"
            )?.value || 0.75
        );


    // ========================================================
    // COMBINE EVERYTHING RETURNED BY BACKEND
    // ========================================================

    const allTickets = [

        ...(currentResults.highConfidence || []),

        ...(currentResults.lowConfidence || [])

    ];


    const high = [];

    const low = [];


    // ========================================================
    // CLASSIFY ALL RETURNED TICKETS
    // ========================================================
    //
    // IMPORTANT:
    //
    // We DO NOT use lowThreshold here.
    //
    // Every ticket returned by PostgreSQL is displayed.
    //
    // Only the high-confidence threshold decides
    // whether a ticket goes into HIGH or LOW.
    //
    // ========================================================

    allTickets.forEach(
        ticket => {

            const similarity =
                Number(
                    ticket.similarity || 0
                );


            if (
                similarity >=
                highThreshold
            ) {

                high.push(
                    ticket
                );

            } else {

                low.push(
                    ticket
                );
            }
        }
    );


    // ========================================================
    // TOTAL COUNT
    // ========================================================

    const resultCount =
        getElement(
            "resultCount"
        );


    if (resultCount) {

        resultCount.textContent =
            `${allTickets.length} tickets`;
    }


    // ========================================================
    // HIGH COUNT
    // ========================================================

    const highCount =
        getElement(
            "highCount"
        );


    if (highCount) {

        highCount.textContent =
            high.length;
    }


    // ========================================================
    // LOW COUNT
    // ========================================================

    const lowCount =
        getElement(
            "lowCount"
        );


    if (lowCount) {

        lowCount.textContent =
            low.length;
    }


    // ========================================================
    // RENDER HIGH
    // ========================================================

    renderTicketList(
        "highResults",
        high
    );


    // ========================================================
    // RENDER LOW
    // ========================================================

    renderTicketList(
        "lowResults",
        low
    );
}

// ============================================================
// UPDATE THRESHOLD LABELS
// ============================================================

function updateThresholdLabels() {

    const high =
        getElement("highThreshold");


    const low =
        getElement("lowThreshold");


    const highValue =
        getElement("highValue");


    const lowValue =
        getElement("lowValue");


    if (high && highValue) {

        highValue.textContent =
            Number(
                high.value
            ).toFixed(2);
    }


    if (low && lowValue) {

        lowValue.textContent =
            Number(
                low.value
            ).toFixed(2);
    }
}


// ============================================================
// LOADING STATE
// ============================================================

function setLoading(
    loading
) {

    if (loading) {

        show(
            "loading"
        );

    } else {

        hide(
            "loading"
        );
    }
}


// ============================================================
// SHOW ELEMENT
// ============================================================

function show(
    id
) {

    const element =
        getElement(id);


    if (!element) {
        return;
    }


    element.classList.remove(
        "hidden"
    );
}


// ============================================================
// HIDE ELEMENT
// ============================================================

function hide(
    id
) {

    const element =
        getElement(id);


    if (!element) {
        return;
    }


    element.classList.add(
        "hidden"
    );
}


// ============================================================
// CLEAN DESCRIPTION PREVIEW
// ============================================================

function cleanPreview(
    text
) {

    return String(text)
        .replace(/\s+/g, " ")
        .trim()
        .substring(
            0,
            180
        );
}


// ============================================================
// ESCAPE HTML
// ============================================================
//
// Very important because ticket data comes from PostgreSQL.
//
// This prevents ticket text from being interpreted as HTML.
// ============================================================

function escapeHtml(
    value
) {

    return String(
        value ?? ""
    )
        .replace(
            /&/g,
            "&amp;"
        )
        .replace(
            /</g,
            "&lt;"
        )
        .replace(
            />/g,
            "&gt;"
        )
        .replace(
            /"/g,
            "&quot;"
        )
        .replace(
            /'/g,
            "&#039;"
        );
}


// ============================================================
// MODAL ESCAPE KEY
// ============================================================

document.addEventListener(
    "keydown",
    event => {

        if (
            event.key === "Escape"
        ) {

            closeTicket();
        }
    }
);
// ============================================================
// EMBEDDING HEALTH
// ============================================================
//
// These functions monitor the embedding pipeline.
//
// APIs:
//
// GET  /tickets/embedding/health
// GET  /tickets/embedding/failed
// POST /tickets/{ticketId}/embedding/retry
//
// ============================================================


// ============================================================
// LOAD EMBEDDING HEALTH
// ============================================================

async function loadEmbeddingHealth() {

    try {

        console.log(
            "Loading embedding health..."
        );


        // ----------------------------------------------------
        // GET HEALTH
        // ----------------------------------------------------

        const healthResponse =
            await fetch(
                "/tickets/embedding/health",
                {
                    method: "GET",

                    headers: {
                        "Accept":
                            "application/json"
                    }
                }
            );


        if (!healthResponse.ok) {

            throw new Error(
                `Health API returned HTTP ${healthResponse.status}`
            );
        }


        const health =
            await healthResponse.json();


        console.log(
            "Embedding health:",
            health
        );


        // ----------------------------------------------------
        // UPDATE HEALTH CARDS
        // ----------------------------------------------------

        const total =
            getElement("totalTickets");

        const embedded =
            getElement("embeddedTickets");

        const pending =
            getElement("pendingTickets");

        const failed =
            getElement("failedTickets");


        if (total) {

            total.textContent =
                health.total ?? 0;
        }


        if (embedded) {

            embedded.textContent =
                health.embedded ?? 0;
        }


        if (pending) {

            pending.textContent =
                health.pending ?? 0;
        }


        if (failed) {

            failed.textContent =
                health.failed ?? 0;
        }


        // ----------------------------------------------------
        // LOAD FAILED TICKETS
        // ----------------------------------------------------

        await loadFailedEmbeddings();


    } catch (error) {

        console.error(
            "Unable to load embedding health:",
            error
        );

    }
}


// ============================================================
// LOAD FAILED EMBEDDINGS
// ============================================================

async function loadFailedEmbeddings() {

    try {

        console.log(
            "Loading failed embeddings..."
        );


        const response =
            await fetch(
                "/tickets/embedding/failed",
                {
                    method: "GET",

                    headers: {
                        "Accept":
                            "application/json"
                    }
                }
            );


        if (!response.ok) {

            throw new Error(
                `Failed embeddings API returned HTTP ${response.status}`
            );
        }


        const failedTickets =
            await response.json();


        console.log(
            "Failed embedding tickets:",
            failedTickets
        );


        renderFailedEmbeddings(
            Array.isArray(failedTickets)
                ? failedTickets
                : []
        );


    } catch (error) {

        console.error(
            "Unable to load failed embeddings:",
            error
        );


        renderFailedEmbeddings([]);
    }
}


// ============================================================
// RENDER FAILED EMBEDDINGS
// ============================================================

function renderFailedEmbeddings(
    tickets
) {

    const container =
        getElement(
            "failedEmbeddingsList"
        );


    const count =
        getElement(
            "failedEmbeddingCount"
        );


    if (!container) {

        return;
    }


    // --------------------------------------------------------
    // UPDATE COUNT
    // --------------------------------------------------------

    if (count) {

        count.textContent =
            tickets.length;
    }


    // --------------------------------------------------------
    // NO FAILED TICKETS
    // --------------------------------------------------------

    if (!tickets.length) {

        container.innerHTML = `

            <p class="no-failed-tickets">
                No failed embeddings.
            </p>

        `;

        return;
    }


    // --------------------------------------------------------
    // RENDER FAILED TICKETS
    // --------------------------------------------------------

    container.innerHTML = "";


    tickets.forEach(
        ticket => {

            const card =
                createFailedEmbeddingCard(
                    ticket
                );


            container.appendChild(
                card
            );
        }
    );
}


// ============================================================
// CREATE FAILED EMBEDDING CARD
// ============================================================

function createFailedEmbeddingCard(
    ticket
) {

    const card =
        document.createElement("div");


    card.className =
        "failed-embedding-card";


    const ticketId =
        ticket.ticketId ||
        "Unknown Ticket";


    const title =
        ticket.title &&
        String(ticket.title).trim()
            ? ticket.title
            : "Untitled ticket";


    const attempts =
        ticket.embeddingAttempts ??
        0;

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


    // --------------------------------------------------------
    // OPEN TICKET
    // --------------------------------------------------------

    const ticketButton =
        card.querySelector(
            ".failed-embedding-id"
        );


    if (ticketButton) {

        ticketButton.addEventListener(
            "click",
            () => {

                openTicket(
                    ticketId
                );

            }
        );
    }


    // --------------------------------------------------------
    // RETRY EMBEDDING
    // --------------------------------------------------------

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


    // --------------------------------------------------------
    // DISABLE BUTTON
    // --------------------------------------------------------

    if (button) {

        button.disabled =
            true;

        button.textContent =
            "Retrying...";
    }


    try {

        console.log(
            "Retrying embedding:",
            ticketId
        );


        const response =
            await fetch(
                `/tickets/${encodeURIComponent(ticketId)}/embedding/retry`,
                {
                    method: "POST",

                    headers: {
                        "Accept":
                            "text/plain"
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


        // ----------------------------------------------------
        // REFRESH HEALTH
        // ----------------------------------------------------

        await loadEmbeddingHealth();


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

            button.disabled =
                false;

            button.textContent =
                "Retry";
        }
    }
}

//--------------------------

// ==========================================
// PRODUCT SUPPORT ANALYTICS
// ==========================================

async function loadProductAnalytics() {

    const container =
        document.getElementById("productAnalyticsList");

    const emptyState =
        document.getElementById("productAnalyticsEmpty");


    if (!container) {
        return;
    }


    try {

        container.innerHTML = `
            <div class="product-analytics-loading">
                Loading product analytics...
            </div>
        `;


        const response =
            await fetch("/tickets/analytics/products", {
                method: "GET",
                headers: {
                    "Accept": "application/json"
                }
            });


        if (!response.ok) {

            throw new Error(
                `HTTP ${response.status}`
            );
        }


        const products =
            await response.json();


        if (!Array.isArray(products)) {

            throw new Error(
                "Invalid product analytics response"
            );
        }


        // ==========================================
        // NO DATA
        // ==========================================

        if (products.length === 0) {

            container.innerHTML = "";

            if (emptyState) {
                emptyState.classList.remove("hidden");
            }

            return;
        }


        if (emptyState) {
            emptyState.classList.add("hidden");
        }


        renderProductAnalytics(
            products,
            container
        );

    } catch (error) {

        console.error(
            "Failed to load product analytics:",
            error
        );


        container.innerHTML = `
            <div class="product-analytics-error">
                Unable to load product analytics.
            </div>
        `;

    }
}


// ==========================================
// RENDER PRODUCT ANALYTICS
// ==========================================

function renderProductAnalytics(
    products,
    container
) {

    container.innerHTML = "";


    // Find the largest count.
    // We use this to calculate the progress-bar width.

    const maxCount =
        Math.max(
            ...products.map(
                product => product.queryCount
            )
        );


    products.forEach(
        (product, index) => {

            const card =
                createProductAnalyticsCard(
                    product,
                    index,
                    maxCount
                );

            container.appendChild(card);
        }
    );
}


// ==========================================
// CREATE PRODUCT CARD
// ==========================================

function createProductAnalyticsCard(
    product,
    index,
    maxCount
) {

    const card =
        document.createElement("div");

    card.className =
        "product-analytics-card";


    const productName =
        product.product || "Unknown Product";


    const queryCount =
        Number(product.queryCount) || 0;


    const percentage =
        maxCount > 0
            ? (queryCount / maxCount) * 100
            : 0;


    card.innerHTML = `

        <div class="product-analytics-info">

            <div class="product-analytics-rank">
                ${index + 1}
            </div>


            <div class="product-analytics-name">
                ${escapeHtml(productName)}
            </div>

        </div>


        <div class="product-analytics-bar-container">

            <div
                class="product-analytics-bar"
                style="width: ${percentage}%;">
            </div>

        </div>


        <div class="product-analytics-count">

            ${queryCount.toLocaleString()}

            <span>
                ${queryCount === 1 ? "query" : "queries"}
            </span>

        </div>

    `;


    return card;
}

// ============================================================
// SEARCH RELATED TICKETS FROM CURRENT CONVERSATION
// ============================================================

function searchRelatedTicketsFromConversation() {

    const modalBody =
        getElement("modalBody");

    const searchInput =
        getElement("searchInput");


    if (!modalBody || !searchInput) {

        console.error(
            "Unable to find modal body or search input."
        );

        return;
    }


    // --------------------------------------------------------
    // Find the conversation box
    // --------------------------------------------------------

    const conversationBox =
        modalBody.querySelector(
            ".conversation-box"
        );


    if (!conversationBox) {

        alert(
            "No conversation is available for this ticket."
        );

        return;
    }


    // --------------------------------------------------------
    // Extract only the conversation text
    // --------------------------------------------------------

    const conversation =
        conversationBox.innerText.trim();


    if (!conversation) {

        alert(
            "No conversation is available for this ticket."
        );

        return;
    }


    // --------------------------------------------------------
    // Put conversation into search box
    // --------------------------------------------------------

    searchInput.value =
        conversation;


    // --------------------------------------------------------
    // Close current ticket modal
    // --------------------------------------------------------

    closeTicket();


    // --------------------------------------------------------
    // Run RAG search automatically
    // --------------------------------------------------------

    searchTickets();
}