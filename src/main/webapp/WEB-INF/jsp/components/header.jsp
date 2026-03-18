<%@ page contentType="text/html;charset=UTF-8" language="java" %>
<header class="flex items-center justify-between whitespace-nowrap border-b border-primary/10 bg-white px-6 py-3 lg:px-10 sticky top-0 z-50">
    <div class="flex items-center gap-8">
        <a href="${pageContext.request.contextPath}/index.jsp" class="flex items-center gap-3">
            <div class="flex items-center justify-center w-8 h-8 rounded-lg bg-primary text-white">
                <span class="material-symbols-outlined text-xl">work</span>
            </div>
            <div class="flex items-center gap-1">
                <span class="font-black text-lg tracking-tighter text-primary">QM</span>
                <span class="font-light text-lg tracking-tight text-slate-500">HIRE</span>
            </div>
        </a>
        <div class="hidden md:flex flex-col min-w-64">
            <div class="flex w-full items-stretch rounded h-10">
                <div class="text-slate-500 flex border border-primary/10 border-r-0 bg-slate-50 items-center justify-center pl-4 rounded-l">
                    <span class="material-symbols-outlined !text-xl">search</span>
                </div>
                <input class="form-input w-full border border-primary/10 border-l-0 bg-slate-50 text-slate-900 focus:ring-0 focus:border-primary/20 rounded-r text-sm px-2 outline-none" placeholder="Search vacancies, applications..." />
            </div>
        </div>
    </div>
    <div class="flex items-center gap-4">
        <div class="flex gap-2">
            <button class="flex items-center justify-center rounded-lg h-10 w-10 bg-slate-50 text-slate-600 hover:bg-primary/10 transition-colors">
                <span class="material-symbols-outlined">notifications</span>
            </button>
            <button class="flex items-center justify-center rounded-lg h-10 w-10 bg-slate-50 text-slate-600 hover:bg-primary/10 transition-colors">
                <span class="material-symbols-outlined">help</span>
            </button>
        </div>
        <div class="h-8 w-[1px] bg-slate-200 mx-1"></div>
        <div class="flex items-center gap-3">
            <div class="flex flex-col items-end hidden sm:flex">
                <span class="text-sm font-semibold">Alex Thompson</span>
                <span class="text-xs text-slate-500">Senior Designer</span>
            </div>
            <div class="bg-primary/10 rounded-full border border-primary/20 overflow-hidden size-10">
                <img alt="User" class="w-full h-full object-cover" src="https://lh3.googleusercontent.com/aida-public/AB6AXuDHwL-Uc_yQBmgx-KNptoBLRU4C3LiwZJ__8HWn1eeCKC3PuGkDH7JpWOZhoHOKYDM_65wh3H3tBi4MyUTaoybvBU-H6r4eN8JB2K4xmWb1wirape71w8tk4goH39t8la7DgipLBCKb6mZSJItbiAXSnhaO4eqfI-bTF_m-26DSHq6ha9pcOwgSuMkRxVzsJHiBQURs22714xz-bAcViRuoV-4eLJBpqrjaxIkvSzgJe7zElIDywn-wsXLG5q-9DcbYxY_xrgLJBSQ" />
            </div>
        </div>
    </div>
</header>
