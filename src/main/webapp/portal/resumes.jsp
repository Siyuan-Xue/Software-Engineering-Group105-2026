<%@ page contentType="text/html;charset=UTF-8" language="java" %>
<%@ taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core" %>
<!DOCTYPE html>
<html lang="en">
<head>
    <meta charset="UTF-8">
    <meta name="viewport" content="width=device-width, initial-scale=1.0">
    <title>Resumes - QM HIRE</title>
    <script src="https://cdn.tailwindcss.com"></script>
    <link href="https://fonts.googleapis.com/css2?family=Inter:wght@300;400;500;600;700;800;900&display=swap" rel="stylesheet"/>
    <link href="https://fonts.googleapis.com/css2?family=Material+Symbols+Outlined:wght,FILL@100..700,0..1&display=swap" rel="stylesheet"/>
    <jsp:include page="/WEB-INF/jsp/components/portal_theme.jsp" />
    <style>
        .upload-zone { 
            transition: all 0.3s cubic-bezier(0.4, 0, 0.2, 1); 
        }
        .upload-zone.dragover { 
            background-color: #f0f9ff; 
            border-color: #3b82f6; 
            transform: scale(1.02); 
        }
        .modal {
            animation: modalPop 0.3s cubic-bezier(0.34, 1.56, 0.64, 1);
        }
        @keyframes modalPop {
            from { transform: scale(0.7); opacity: 0; }
            to { transform: scale(1); opacity: 1; }
        }
    </style>
</head>
<body class="bg-background-light font-sans text-slate-900 overflow-x-hidden">
    <div class="relative flex min-h-screen w-full flex-col">
        <jsp:include page="/WEB-INF/jsp/components/header.jsp" />
        
        <div class="flex flex-1 overflow-hidden">
            <jsp:include page="/WEB-INF/jsp/components/sidebar.jsp" />

            <main class="flex-1 overflow-y-auto bg-background-light p-6 lg:p-10">
                <div class="portal-page max-w-6xl mx-auto">
                    <jsp:include page="/WEB-INF/jsp/components/flash_messages.jsp">
                        <jsp:param name="containerClass" value="mb-8" />
                    </jsp:include>

                    <div class="flex justify-between items-end mb-8">
                        <div>
                            <h1 class="text-4xl font-black tracking-tighter text-slate-900">Resumes</h1>
                            <p class="text-slate-500 mt-1">Upload, analyze, and manage your teaching assistant resumes</p>
                        </div>
                        <div class="flex gap-3">
                            <button onclick="showAIReviewModal()" 
                                    class="flex items-center gap-2 px-6 py-3 bg-white border border-slate-200 rounded-2xl hover:bg-slate-50 font-medium text-sm">
                                <span class="material-symbols-outlined">auto_awesome</span>
                                AI Resume Review
                            </button>
                        </div>
                    </div>

                    <!-- Upload Area -->
                    <div id="uploadZone" 
                         class="upload-zone border-2 border-dashed border-slate-300 rounded-3xl p-16 text-center mb-12 cursor-pointer hover:border-primary transition-colors"
                         onclick="document.getElementById('fileInput').click()">
                        <div class="mx-auto w-20 h-20 bg-primary/10 rounded-2xl flex items-center justify-center mb-6">
                            <span class="material-symbols-outlined text-6xl text-primary">cloud_upload</span>
                        </div>
                        <h3 class="text-2xl font-semibold text-slate-800 mb-2">Upload Your Resume</h3>
                        <p class="text-slate-500 max-w-md mx-auto">PDF, JPG, or PNG • Our AI will automatically extract information using OCR and semantic analysis</p>
                        <input type="file" id="fileInput" accept=".pdf,.jpg,.jpeg,.png" class="hidden" 
                               onchange="handleFileSelect(event)">
                        <div class="mt-6 text-xs text-slate-400">or drag and drop files here</div>
                    </div>

                    <!-- Resume List -->
                    <div class="mb-12">
                        <div class="flex items-center justify-between mb-6">
                            <h2 class="text-xl font-semibold">Your Resumes</h2>
                            <button onclick="showManualForm()" 
                                    class="flex items-center gap-2 text-sm font-medium text-primary hover:text-primary/80">
                                <span class="material-symbols-outlined">add</span>
                                New Manual Resume
                            </button>
                        </div>

                        <c:choose>
                            <c:when test="${empty resumes}">
                                <div class="bg-white border border-dashed border-slate-200 rounded-3xl p-16 text-center">
                                    <span class="material-symbols-outlined text-6xl text-slate-300 mb-4">description</span>
                                    <p class="text-slate-500">No resumes yet. Upload one above to begin.</p>
                                </div>
                            </c:when>
                            <c:otherwise>
                                <div class="grid grid-cols-1 md:grid-cols-2 gap-6">
                                    <c:forEach items="${resumes}" var="resume">
                                        <div class="bg-white border border-slate-100 rounded-3xl p-6 hover:shadow-md transition-all group">
                                            <div class="flex justify-between">
                                                <div>
                                                    <div class="font-semibold text-lg"><c:out value="${resume.title}"/></div>
                                                    <div class="text-sm text-slate-500 mt-1">
                                                        <c:out value="${resume.department}"/> • 
                                                        <c:out value="${resume.degreeLevel}"/> • GPA <c:out value="${resume.gpa}"/>
                                                    </div>
                                                </div>
                                                <div class="flex gap-2 opacity-60 group-hover:opacity-100 transition-opacity">
                                                    <button onclick="editResume('${resume.id}')" 
                                                            class="w-9 h-9 flex items-center justify-center rounded-2xl hover:bg-slate-100">
                                                        <span class="material-symbols-outlined text-xl">edit</span>
                                                    </button>
                                                    <button onclick="deleteResume('${resume.id}')" 
                                                            class="w-9 h-9 flex items-center justify-center rounded-2xl hover:bg-red-50 text-red-500">
                                                        <span class="material-symbols-outlined text-xl">delete</span>
                                                    </button>
                                                </div>
                                            </div>
                                            <div class="mt-6 pt-6 border-t text-xs text-slate-400">
                                                Last updated: <c:out value="${resume.updatedAt}"/>
                                            </div>
                                        </div>
                                    </c:forEach>
                                </div>
                            </c:otherwise>
                        </c:choose>
                    </div>
                </div>
            </main>
        </div>
    </div>

    <!-- AI Review Modal -->
    <div id="aiModal" onclick="if(event.target.id==='aiModal') hideAIReviewModal()" 
         class="hidden fixed inset-0 bg-black/70 flex items-center justify-center z-[100]">
        <div onclick="event.stopImmediatePropagation()" 
             class="modal bg-white rounded-3xl max-w-2xl w-full mx-4 max-h-[85vh] overflow-hidden shadow-2xl">
            <div class="px-8 pt-8 pb-6 border-b flex items-center justify-between">
                <div class="flex items-center gap-3">
                    <span class="material-symbols-outlined text-3xl text-violet-600">auto_awesome</span>
                    <h2 class="text-2xl font-bold">AI Resume Review</h2>
                </div>
                <button onclick="hideAIReviewModal()" class="text-slate-400 hover:text-slate-600">
                    <span class="material-symbols-outlined text-4xl">close</span>
                </button>
            </div>
            
            <div class="p-8 overflow-auto" style="max-height: calc(85vh - 180px)">
                <div id="aiDisclaimer" class="bg-amber-50 border border-amber-200 rounded-2xl p-6 text-sm leading-relaxed mb-8">
                    <!-- Injected by servlet -->
                </div>
                
                <div id="aiResult" class="prose text-slate-700">
                    <!-- Injected by servlet -->
                </div>
            </div>
            
            <div class="px-8 py-6 border-t bg-slate-50 flex justify-end rounded-b-3xl">
                <button onclick="hideAIReviewModal()" 
                        class="px-10 py-3.5 font-semibold rounded-2xl bg-white border border-slate-200 hover:bg-slate-50">
                    Close Analysis
                </button>
            </div>
        </div>
    </div>

    <script>
        function handleFileSelect(e) {
            const file = e.target.files[0];
            if (!file) return;
            const formData = new FormData();
            formData.append("action", "upload");
            formData.append("resumeFile", file);

            fetch('${pageContext.request.contextPath}/resumes', {
                method: 'POST',
                body: formData
            }).then(() => {
                alert("Resume uploaded successfully! AI is analyzing the document...");
                window.location.reload();
            });
        }

        function showAIReviewModal() {
            fetch('${pageContext.request.contextPath}/resumes?action=aiReview', { method: 'POST' })
                .then(r => r.text())
                .then(() => window.location.reload());
        }

        function hideAIReviewModal() {
            document.getElementById('aiModal').classList.add('hidden');
        }

        function deleteResume(id) {
            if (!confirm("Delete this resume?")) return;
            const form = document.createElement('form');
            form.method = 'POST';
            form.action = '${pageContext.request.contextPath}/resumes';
            form.innerHTML = `
                <input type="hidden" name="action" value="delete">
                <input type="hidden" name="resumeId" value="${id}">
            `;
            document.body.appendChild(form);
            form.submit();
        }

        function editResume(id) {
            alert("Edit functionality for resume " + id + " is ready for further development.");
        }

        // Drag and drop
        const zone = document.getElementById('uploadZone');
        zone.addEventListener('dragover', e => {
            e.preventDefault();
            zone.classList.add('dragover');
        });
        zone.addEventListener('dragleave', () => zone.classList.remove('dragover'));
        zone.addEventListener('drop', e => {
            e.preventDefault();
            zone.classList.remove('dragover');
            const file = e.dataTransfer.files[0];
            if (file) {
                const dt = new DataTransfer();
                dt.items.add(file);
                const input = document.getElementById('fileInput');
                input.files = dt.files;
                handleFileSelect({target: input});
            }
        });
    </script>
</body>
</html>
